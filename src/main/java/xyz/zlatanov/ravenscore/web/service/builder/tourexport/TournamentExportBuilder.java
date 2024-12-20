package xyz.zlatanov.ravenscore.web.service.builder.tourexport;

import static xyz.zlatanov.ravenscore.Utils.contains;

import java.util.List;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.web.model.export.*;

@RequiredArgsConstructor
public class TournamentExportBuilder {

	private final Tournament			tournament;
	private final List<TournamentStage>	tournamentStageList;
	private final List<Substitute>		substituteList;
	private final List<Participant>		participantList;
	private final List<Game>			gameList;
	private final List<Player>			playerList;

	public TournamentExport build() {
		return new TournamentExport()
				.name(tournament.name())
				.description(tournament.description())
				.scoring(tournament.scoring())
				.hidden(tournament.hidden())
				.tournamentKey(tournament.tournamentKey())
				.startDate(tournament.startDate())
				.substituteExportList(substituteList.stream()
						.map(s -> new SubstituteExport()
								.name(s.name())
								.profileLinks(s.profileLinks()))
						.toList())
				.tournamentStageExportList(tournamentStageList.stream()
						.map(ts -> new TournamentStageExport()
								.name(ts.name())
								.qualificationCount(ts.qualificationCount())
								.startDate(ts.startDate())
								.participantExportList(participantList.stream()
										.filter(p -> contains(ts.participantIdList(), p.id()))
										.map(p -> new ParticipantExport()
												.name(p.name())
												.profileLinks(p.profileLinks())
												.replacementParticipantName(p.replacementParticipantId() == null ? null
														: participantList.stream()
																.filter(pp -> pp.id().equals(p.replacementParticipantId()))
																.findFirst()
																.map(Participant::name)
																.orElse(null)))
										.toList())
								.gameExportList(gameList.stream()
										.map(g -> new GameExport()
												.name(g.name())
												.type(g.type())
												.link(g.link())
												.round(g.round())
												.completed(g.completed())
												.participantNameList(participantList.stream()
														.filter(p -> contains(g.participantIdList(), p.id()))
														.map(Participant::name)
														.toList())
												.playerExportList(playerList.stream()
														.filter(pl -> pl.gameId().equals(g.id()))
														.map(pl -> new PlayerExport()
																.house(pl.house())
																.castles(pl.castles())
																.points(pl.points())
																.penaltyPoints(pl.penaltyPoints())
																.participantName(pl.participantId() == null ? null
																		: participantList.stream()
																				.filter(part -> part.id().equals(pl.participantId()))
																				.map(Participant::name)
																				.findFirst()
																				.orElse(null)))
														.toList()))
										.toList()))
						.toList());
	}
}
