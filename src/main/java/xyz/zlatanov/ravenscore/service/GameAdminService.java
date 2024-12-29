package xyz.zlatanov.ravenscore.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Game;
import xyz.zlatanov.ravenscore.domain.domain.Player;
import xyz.zlatanov.ravenscore.domain.repository.GameRepository;
import xyz.zlatanov.ravenscore.domain.repository.PlayerRepository;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.PlayerRanking;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.RankingsForm;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

@Service
@RequiredArgsConstructor
public class GameAdminService {

	private final GameRepository	gameRepository;
	private final PlayerRepository	playerRepository;

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
}
