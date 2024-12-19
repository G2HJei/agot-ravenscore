package xyz.zlatanov.ravenscore.web.service.builder;

import static java.math.RoundingMode.UP;
import static xyz.zlatanov.ravenscore.Utils.*;

import java.math.BigDecimal;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.*;

@RequiredArgsConstructor
public class TournamentDetailsBuilder {

	private final boolean				adminUnlocked;
	private final Tournament			tournament;
	private final List<TournamentStage>	tournamentStageList;
	private final List<Substitute>		substituteList;
	private final List<Participant>		participantList;
	private final List<Game>			gameList;
	private final List<Player>			playerList;

	public TournamentDetailsModel build() {
		val tournamentStageModelList = getTournamentStages();
		return new TournamentDetailsModel()
				.id(tournament.id().toString())
				.name(tournament.name())
				.description(tournament.description())
				.hidden(tournament.hidden())
				.scoring(tournament.scoring())
				.startDate(DATE_FORMATTER.format(tournament.startDate()))
				.adminUnlocked(adminUnlocked)
				.tournamentKey(tournament.tournamentKey())
				.winnerParticipantId(getWinnerId(tournamentStageModelList))
				.substituteModelList(getSubstitutes())
				.tournamentStageModelList(tournamentStageModelList);
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
				.map(stage -> {
					val gameModelList = getGames(stage.id());
					return new TournamentStageModel()
							.id(stage.id().toString())
							.name(stage.name())
							.qualificationCount(stage.qualificationCount())
							.startDate(DATE_FORMATTER.format(stage.startDate()))
							.completed(!gameModelList.isEmpty() && gameModelList.stream().allMatch(GameModel::completed))
							.participantModelList(getParticipants(stage))
							.gameModelList(gameModelList);
				})
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
					val penaltyPoints = players.stream().map(Player::penaltyPoints).reduce(Integer::sum).orElse(0);
					val participantWins = calculateWins(participant.id(), gameList);
					return new ParticipantModel()
							.id(participant.id().toString())
							.name(participant.name())
							.replacedLabel(getReplacedLabel(participant.id()))
							.profileLinks(Arrays.stream(participant.profileLinks()).map(ProfileLink::new).toList())
							.games(completedGames)
							.points(points)
							.penaltyPoints(penaltyPoints)
							.wins(participantWins.size())
							.cleanWins((int) participantWins.stream().filter(GameWin::clean).count())
							.avgPoints(completedGames == 0 ? null
									: DECIMAL_FORMATTER
											.format(BigDecimal.valueOf(points - penaltyPoints)
													.divide(BigDecimal.valueOf(completedGames), 2, UP)));
				})
				.sorted((o1, o2) -> {
					int scoreComparison = -o1.score().compareTo(o2.score());
					if (scoreComparison != 0) {
						return scoreComparison;
					}
					int winsComparison = Integer.compare(o2.wins(), o1.wins());
					if (winsComparison != 0) {
						return winsComparison;
					}
					return Integer.compare(o2.cleanWins(), o1.cleanWins());
				}).toList();
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
									.orElseThrow());
					return new PlayerModel()
							.id(player.id().toString())
							.name(participant.map(Participant::name).orElse(capitalizeFirstLetter(player.house())))
							.replacedLabel(Optional.ofNullable(player.participantId()).map(this::getReplacedLabel).orElse(null))
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
							.score(player.points())
							.penaltyPoints(player.penaltyPoints())
							.participantId(Optional.ofNullable(player.participantId()).map(Object::toString).orElse(null));
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
					.map(Player::points)
					.reduce(Integer::sum)
					.orElse(0);
		}

	}

	private String getReplacedLabel(UUID participantId) {
		val replacedNames = findReplacementNames(participantId);
		return replacedNames.isEmpty() ? null : "Replaced " + String.join(", ", replacedNames);
	}

	private List<String> findReplacementNames(UUID participantId) {
		for (val participant : participantList) {
			if (participantId.equals(participant.replacementParticipantId())) {
				val names = findReplacementNames(participant.id());
				names.add(participant.name());
				return names;
			}
		}
		return new ArrayList<>();
	}

	private List<GameWin> calculateWins(UUID participantId, List<Game> gameList) {
		return gameList.stream()
				.filter(Game::completed)
				.map(game -> {
					val players = playerList.stream()
							.filter(p -> p.gameId().equals(game.id()))
							.sorted((o1, o2) -> -o1.points().compareTo(o2.points()))
							.toList();
					val winner = players.getFirst();
					val second = players.get(1);
					return winner.participantId().equals(participantId)
							? new GameWin().clean(winner.castles() > second.castles())
							: null;
				})
				.filter(Objects::nonNull)
				.toList();
	}

	private String getWinnerId(List<TournamentStageModel> tournamentStageModelList) {
		if (tournamentStageList.isEmpty() || tournamentStageModelList.stream().anyMatch(s -> !s.completed())) {
			return null;
		}
		val lastStage = tournamentStageModelList.getFirst();
		if (lastStage.qualificationCount() != 1) {
			return null;
		}
		return lastStage.participantModelList().getFirst().id();
	}
}