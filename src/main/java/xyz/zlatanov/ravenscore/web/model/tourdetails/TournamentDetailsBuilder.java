package xyz.zlatanov.ravenscore.web.model.tourdetails;

import static java.math.RoundingMode.UP;
import static xyz.zlatanov.ravenscore.Utils.*;

import java.math.BigDecimal;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.web.service.RavenscoreException;

@RequiredArgsConstructor
public class TournamentDetailsBuilder {

	private final Tournament			tournament;
	private final String				adminKeyHash;
	private final List<TournamentStage>	tournamentStageList;
	private final List<Substitute>		substituteList;
	private final List<Participant>		participantList;
	private final List<Game>			gameList;
	private final List<Player>			playerList;

	public TournamentDetailsModel build() {
		return new TournamentDetailsModel()
				.id(tournament.id().toString())
				.name(tournament.name())
				.description(tournament.description())
				.scoring(tournament.scoring())
				.startDate(DATE_FORMATTER.format(tournament.startDate()))
				.adminUnlocked(tournament.validateUnlockHash(adminKeyHash))
				.substituteModelList(getSubstitutes())
				.tournamentStageModelList(getTournamentStages());
	}

	private List<SubstituteModel> getSubstitutes() {
		return substituteList.stream()
				.map(sub -> new SubstituteModel()
						.id(sub.id().toString())
						.name(sub.name())
						.profileLinks(Arrays.stream(Optional.ofNullable(sub.profileLinks()).orElse(new String[] {}))
								.map(ProfileLink::new)
								.sorted(Comparator.comparing(ProfileLink::link, Comparator.nullsFirst(Comparator.naturalOrder())))
								.toList()))
				.toList();
	}

	private List<TournamentStageModel> getTournamentStages() {
		return tournamentStageList.stream()
				.filter(stage -> stage.tournamentId().equals(tournament.id()))
				.map(stage -> new TournamentStageModel()
						.id(stage.id().toString())
						.name(stage.name())
						.qualificationCount(stage.qualificationCount())
						.startDate(DATE_FORMATTER.format(stage.startDate()))
						.participantModelList(getParticipants(stage))
						.gameModelList(getGames(stage.id())))
				.toList();
	}

	private List<ParticipantModel> getParticipants(TournamentStage stage) {
		val participantIds = Arrays.stream(stage.participantIdList()).toList();
		return participantList.stream()
				.filter(p -> p.replacementParticipantId() == null)
				.filter(p -> participantIds.contains(p.id()))
				.map(participant -> {
					val games = gameList.stream()
							.filter(game -> Arrays.stream(game.participantIdList()).toList().contains(participant.id()))
							.toList();
					val players = playerList.stream()
							.filter(player -> participant.id().equals(player.participantId()))
							.toList();
					val completedGames = games.stream().filter(Game::completed).toList().size();
					val points = new PlayerPointsCalculator(games, players).calculatePoints();
					return new ParticipantModel()
							.id(participant.id().toString())
							.name(participant.name())
							.replacedLabel(getReplacedLabel(participant.id()))
							.profileLinks(Arrays.stream(participant.profileLinks()).map(ProfileLink::new).toList())
							.games(completedGames + "/" + games.size())
							.points(points)
							.penaltyPoints(players.stream().map(Player::penaltyPoints).reduce(Integer::sum).orElse(0))
							.wins(players.stream().map(Player::rank).filter(r -> r == 1).toList().size())
							.avgPoints(completedGames == 0 ? null
									: DECIMAL_FORMATTER
											.format(BigDecimal.valueOf(points).divide(BigDecimal.valueOf(completedGames), 2, UP)));
				})
				.sorted((o1, o2) -> -o1.actualPoints().compareTo(o2.actualPoints()))
				.toList();
	}

	private List<GameModel> getGames(UUID stageId) {
		return gameList.stream()
				.filter(g -> g.tournamentStageId().equals(stageId))
				.map(g -> new GameModel()
						.id(g.id().toString())
						.name(g.name())
						.type(g.type())
						.link(g.link())
						.round(g.round())
						.completed(g.completed())
						.participantIdList(Arrays.stream(g.participantIdList()).map(UUID::toString).toList())
						.playersRevealed(playerList.stream()
								.filter(player -> player.gameId().equals(g.id()))
								.allMatch(player -> player.participantId() != null))
						.playerModelList(getPlayers(g.id())))
				.toList();
	}

	private List<PlayerModel> getPlayers(UUID gameId) {
		return playerList.stream()
				.filter(player -> player.gameId().equals(gameId))
				.map(player -> {
					val participant = Optional.ofNullable(player.participantId())
							.map(participantId -> participantList.stream()
									.filter(p -> p.id().equals(participantId))
									.findFirst()
									.orElseThrow(() -> new RavenscoreException("Invalid player-participant connection.")));
					return new PlayerModel()
							.name(participant.map(Participant::name).orElse(capitalizeFirstLetter(player.house())))
							.profileLinks(participant
									.map(p -> Arrays.stream(p.profileLinks()).toList()
											.stream()
											.map(ProfileLink::new)
											.sorted(Comparator.comparing(ProfileLink::link,
													Comparator.nullsFirst(Comparator.naturalOrder())))
											.toList())
									.orElse(List.of()))
							.house(capitalizeFirstLetter(player.house()))
							.castles(player.castles())
							.score(player.score())
							.penaltyPoints(player.penaltyPoints());
				})
				.toList();
	}

	@RequiredArgsConstructor
	public static class PlayerPointsCalculator {

		private final List<Game>	games;

		private final List<Player>	players;

		public Integer calculatePoints() {
			val gamesMap = new LinkedHashMap<UUID, Game>();
			games.stream()
					.filter(Game::completed)
					.toList()
					.forEach(g -> gamesMap.put(g.id(), g));
			return players.stream()
					.filter(p -> gamesMap.containsKey(p.gameId()))
					// todo this should be in the frontend to autofill score before submitting
					// (gamesMap.get(p.gameId()).participantIdList().length - p.rank() + p.castles() + (p.rank() == 1 ? 3 : 0))
					.map(Player::score)
					.reduce(Integer::sum)
					.orElse(0);
		}

	}

	private String getReplacedLabel(UUID participantId) {
		val replacedParticipantsNames = participantList.stream()
				.filter(p -> Objects.equals(p.replacementParticipantId(), participantId))
				.map(Participant::name)
				.toList();
		return replacedParticipantsNames.isEmpty() ? null
				: "Replaced " + String.join(", ", replacedParticipantsNames);
	}
}
