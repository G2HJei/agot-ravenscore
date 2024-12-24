package xyz.zlatanov.ravenscore.service.builder.tourdetails;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.math.RoundingMode.UP;
import static java.util.Comparator.comparing;
import static xyz.zlatanov.ravenscore.domain.domain.House.BOLTON;
import static xyz.zlatanov.ravenscore.domain.domain.House.STARK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Game;
import xyz.zlatanov.ravenscore.domain.domain.Player;
import xyz.zlatanov.ravenscore.model.statistics.Dataset;
import xyz.zlatanov.ravenscore.model.statistics.HouseData;
import xyz.zlatanov.ravenscore.model.statistics.TournamentStatistics;
import xyz.zlatanov.ravenscore.model.tourdetails.ParticipantModel;
import xyz.zlatanov.ravenscore.model.tourdetails.TournamentStageModel;

@RequiredArgsConstructor
public class TournamentStatisticsBuilder {

	private final List<TournamentStageModel>	tournamentStageModelList;
	private final List<Player>					playerList;
	private final List<Game>					gameList;

	public TournamentStatistics buildStatistics() {
		val houseDataList = calculateHouseData(tournamentStageModelList);
		val stats = new TournamentStatistics().datasets(List.of(
				new Dataset().label("Win percentage"),
				new Dataset().label("Average points")));
		for (val hd : houseDataList) {
			stats.labels().add(hd.label());
			stats.datasets().get(0).data().add(hd.winPercent());
			stats.datasets().get(1).data().add(hd.averagePoints());
		}
		return stats;
	}

	private List<HouseData> calculateHouseData(List<TournamentStageModel> tournamentStageModelList) {
		val houseDataList = new ArrayList<HouseData>();
		playerList.stream()
				.map(Player::house)
				.filter(h -> h != BOLTON) // include in Stark statistics
				.distinct() // each house participating in the tournament game types
				.forEach(house -> {
					val winsCount = tournamentStageModelList.stream()
							.flatMap(tsm -> tsm.participantModelList().stream())
							.map(ParticipantModel::wins)
							.flatMap(Collection::stream)
							.filter(gw -> gw.house() == house || (gw.house() == BOLTON && house == STARK))
							.count();
					val pointsList = gameList.stream()
							.filter(Game::completed)
							.map(g -> playerList.stream()
									.filter(p -> p.gameId().equals(g.id())
											&& (p.house() == house || (p.house() == BOLTON && house == STARK)))
									.findFirst()
									.map(Player::points)
									.orElse(null))
							.filter(Objects::nonNull)
							.toList();
					val pointsSum = pointsList.stream().reduce(Integer::sum).map(BigDecimal::valueOf).orElse(ZERO);
					val gamesCount = BigDecimal.valueOf(pointsList.size());
					val houseData = new HouseData().label(house != STARK ? house.label() : STARK.label() + " / " + BOLTON.label());
					if (gamesCount.compareTo(ZERO) > 0) {
						houseData.winPercent(BigDecimal.valueOf(winsCount).divide(gamesCount, 2, HALF_UP));
						houseData.averagePoints(pointsSum.divide(gamesCount, 2, UP));
					} else {
						houseData.winPercent(ZERO);
						houseData.averagePoints(ZERO);
					}
					houseDataList.add(houseData);
				});
		return houseDataList.stream()
				.sorted(comparing(HouseData::averagePoints))
				.toList()
				.reversed();
	}
}
