package xyz.zlatanov.ravenscore.service;

import static xyz.zlatanov.ravenscore.model.swordsandravens.SnrState.FINISHED;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import xyz.zlatanov.ravenscore.domain.model.Game;
import xyz.zlatanov.ravenscore.domain.model.House;
import xyz.zlatanov.ravenscore.domain.model.Participant;
import xyz.zlatanov.ravenscore.domain.model.Player;
import xyz.zlatanov.ravenscore.domain.repository.GameRepository;
import xyz.zlatanov.ravenscore.domain.repository.ParticipantRepository;
import xyz.zlatanov.ravenscore.domain.repository.PlayerRepository;
import xyz.zlatanov.ravenscore.model.swordsandravens.SnrGameState;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.GameUpdates;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.PlayerRanking;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.RankingsForm;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAdminService {

	private final GameRepository		gameRepository;
	private final PlayerRepository		playerRepository;
	private final ParticipantRepository	participantRepository;

	@Transactional
	@TournamentAdminOperation
	public void game(@Valid GameForm gameForm) {
		validateParticipantCount(gameForm);
		if (gameForm.getId() == null) {
			createGame(gameForm);
		} else {
			updateGame(gameForm);
		}
	}

	@Transactional
	@TournamentAdminOperation
	public void removeGame(UUID gameId) {
		val game = gameRepository.findById(gameId).orElseThrow();
		if (game.completed()) {
			throw new RavenscoreException("Cannot delete a completed game.");
		}
		gameRepository.deleteById(gameId);
	}

	@Transactional
	@TournamentAdminOperation
	public void updateRankings(@Valid RankingsForm rankingsForm) throws RavenscoreException {
		validateRankingsForm(rankingsForm);

		val game = gameRepository.findById(rankingsForm.getGameId()).orElseThrow();
		gameRepository.save(game.completed(rankingsForm.getCompleted()));
		val playerScoringList = rankingsForm.getPlayerRankingList().stream()
				.sorted(Comparator.comparing(PlayerRanking::getPoints))
				.toList();
		playerScoringList.forEach(ps -> {
			val player = playerRepository.findById(ps.getPlayerId()).orElseThrow();
			playerRepository.save(player
					.castles(ps.getCastles())
					.penaltyPoints(ps.getPenaltyPoints())
					.points(ps.getPoints())
					.participantId(ps.getParticipantId()));
		});
	}

	@Transactional
	@TournamentAdminOperation
	public void updateRound(UUID gameId, Integer round) {
		val game = gameRepository.findById(gameId).orElseThrow();
		gameRepository.save(game.round(round));
	}

	private void validateParticipantCount(@Valid GameForm gameForm) {
		val maxNumberOfParticipants = gameForm.getType().houses().size();
		val selectedParticipants = gameForm.getParticipantIdList().length;
		if (selectedParticipants > maxNumberOfParticipants) {
			throw new RavenscoreException(
					String.format("Maximum number of participants for this type of game is %s.", maxNumberOfParticipants));
		}
	}

	private void createGame(GameForm gameForm) {
		val game = gameRepository.save(new Game()
				.type(gameForm.getType())
				.name(gameForm.getName())
				.link(gameForm.getLink())
				.completed(false)
				.tournamentStageId(gameForm.getStageId())
				.participantIdList(gameForm.getParticipantIdList()));
		val defaultPlayers = gameForm.getType().houses()
				.stream()
				.map(house -> new Player()
						.gameId(game.id())
						.house(house))
				.toList();
		playerRepository.saveAll(defaultPlayers);
	}

	private void updateGame(GameForm gameForm) {
		val game = gameRepository.findById(gameForm.getId()).orElseThrow();
		if (game.completed()) {
			throw new RavenscoreException("Cannot update game details of a completed game.");
		}
		gameRepository.save(game
				.type(gameForm.getType())
				.name(gameForm.getName())
				.link(gameForm.getLink())
				.tournamentStageId(gameForm.getStageId())
				.participantIdList(gameForm.getParticipantIdList()));
		// disassociate player from removed participant
		val players = playerRepository.findByGameIdOrderByPointsDesc(game.id()).stream()
				.filter(p -> p.participantId() != null)
				.filter(p -> !participatesInGames(p.participantId(), List.of(game)))
				.map(p -> p.participantId(null))
				.toList();
		playerRepository.saveAll(players);
	}

	private boolean participatesInGames(UUID participantId, List<Game> games) {
		return games.stream().flatMap(g -> Arrays.stream(g.participantIdList())).anyMatch(pId -> pId.equals(participantId));
	}

	private void validateRankingsForm(RankingsForm rankingsForm) {
		if (rankingsForm.getCompleted() && hasUnrevealedPlayers(rankingsForm)) {
			throw new RavenscoreException("Cannot complete game without revealing all players!");
		}
		if (hasDuplicatedParticipants(rankingsForm)) {
			throw new RavenscoreException("Same player selected for more than one house!");
		}
	}

	private boolean hasUnrevealedPlayers(RankingsForm rankingsForm) {
		return rankingsForm.getPlayerRankingList().stream()
				.map(PlayerRanking::getParticipantId)
				.anyMatch(Objects::isNull);
	}

	private boolean hasDuplicatedParticipants(RankingsForm rankingsForm) {
		val participantIdList = rankingsForm.getPlayerRankingList().stream()
				.map(PlayerRanking::getParticipantId)
				.filter(Objects::nonNull)
				.toList();
		val distinctParticipantIdSize = participantIdList.stream().distinct().toList().size();
		return participantIdList.size() != distinctParticipantIdSize;
	}

	public GameUpdates refreshSnrGame(UUID gameId) {
		val game = gameRepository.findById(gameId)
				.orElseThrow(() -> new RavenscoreException("Game not found"));
		val isSnrGame = game.link().toLowerCase().contains("swordsandravens.net");
		if (!isSnrGame) {
			throw new RavenscoreException("This game is not a Swords & Ravens game");
		}
		val snrGameState = getSnrGameState(game);
		return checkForUpdates(game, snrGameState);
	}

	private SnrGameState getSnrGameState(Game game) {
		val snrGameId = game.link()
				.replaceAll(".*swordsandravens\\.net/play/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*", "$1");
		return WebClient.create()
				.get()
				.uri("https://swordsandravens.net/api/public/game/" + snrGameId + "?format=json")
				.retrieve()
				.bodyToMono(SnrGameState.class)
				.block();
	}

	private GameUpdates checkForUpdates(Game game, SnrGameState snrGameState) {
		val snrRound = snrGameState.viewOfGame().round();
		val snrCompleted = snrGameState.state() == FINISHED;
		return new GameUpdates()
				.gameId(game.id())
				.round(!game.round().equals(snrRound) ? snrRound : null)
				.completed(snrCompleted ? true : null)
				.playerRankingList(snrCompleted ? getPlayerRankings(game, snrGameState) : null);
	}

	private ArrayList<PlayerRanking> getPlayerRankings(Game game, SnrGameState snrGameState) {
		val playerRankingList = new ArrayList<PlayerRanking>();
		val players = playerRepository.findByGameIdOrderByPointsDesc(game.id());
		val participants = participantRepository.findByIdInOrderByName(Arrays.stream(game.participantIdList()).toList());
		val victoryTrack = snrGameState.viewOfGame().victoryTrack();
		for (int rank = 1; rank <= victoryTrack.size(); rank++) {
			val snrPlayer = victoryTrack.get(rank - 1);
			val player = findPlayer(players, snrPlayer.house());
			val participant = findParticipant(participants, snrPlayer.name());
			playerRankingList.add(
					new PlayerRanking()
							.setPlayerId(player.id())
							.setHouse(player.house().label())
							.setParticipantId(participant.id())
							.setParticipantName(participant.name())
							.setPoints(victoryTrack.size() - rank + snrPlayer.victoryPoints() + (rank == 1 ? 3 : 0))
							.setCastles(snrPlayer.victoryPoints())
							.setPenaltyPoints(player.penaltyPoints()));
		}
		return playerRankingList;
	}

	private static Participant findParticipant(List<Participant> participants, String playerName) {
		return participants.stream()
				.filter(p -> hasSameNameOrAlias(p, playerName))
				.findFirst()
				.orElseThrow();
	}

	private static boolean hasSameNameOrAlias(Participant participant, String playerName) {
		val playerNameMatch = participant.name().equalsIgnoreCase(playerName);
		val aliasMatch = Arrays.stream(participant.profileLinks())
				.anyMatch(pl -> pl.equalsIgnoreCase(playerName));
		return playerNameMatch || aliasMatch;
	}

	private static Player findPlayer(List<Player> players, House house) {
		return players.stream()
				.filter(p -> p.house() == house)
				.findFirst()
				.orElseThrow();
	}
}
