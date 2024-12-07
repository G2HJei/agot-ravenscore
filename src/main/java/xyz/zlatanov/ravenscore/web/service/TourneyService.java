package xyz.zlatanov.ravenscore.web.service;

import static xyz.zlatanov.ravenscore.Utils.DATE_FORMATTER_PRETTY;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.TournamentSummaryModel;

@Service
@RequiredArgsConstructor
public class TourneyService {

	private final TournamentRepository		tournamentRepository;
	private final SubstituteRepository		substituteRepository;
	private final ParticipantRepository		participantRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional(readOnly = true)
	public List<TournamentSummaryModel> getPublicTourneys() {
		val tourneys = tournamentRepository.findByHiddenFalse();
		val tourIds = tourneys.stream().map(Tournament::id).toList();
		val substitutes = substituteRepository.findByTournamentIdIn(tourIds);
		val stages = tournamentStageRepository.findByTournamentIdIn(tourIds);
		val participantIds = stages.stream().flatMap(ts -> Arrays.stream(ts.participantIdList())).toList();
		val participants = participantRepository.findByIdIn(participantIds);
		val stageIds = stages.stream().map(TournamentStage::id).toList();
		val games = gameRepository.findByTournamentStageIdIn(stageIds);
		val gameIds = games.stream().map(Game::id).toList();
		val players = playerRepository.findByGameIdIn(gameIds);
		return buildModel(tourneys, stages, substitutes, participants, games, players);
	}

	private List<TournamentSummaryModel> buildModel(List<Tournament> tourneys, List<TournamentStage> stages,
			List<Substitute> substitutes, List<Participant> participants, List<Game> games, List<Player> players) {
		return tourneys.stream()
				.map(t -> new TournamentSummaryModel()
						.id(t.id().toString())
						.name(t.name())
						.numberOfParticipants(getNumberOfParticipants(t.id(), stages, participants))
						.startDate(DATE_FORMATTER_PRETTY.format(LocalDateTime.now()))
						.lastStage(getLastStage(t.id(), stages)))
				.toList();
	}

	private Integer getNumberOfParticipants(UUID tourId, List<TournamentStage> stages, List<Participant> participants) {
		val participantIds = stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.flatMap(s -> Arrays.stream(s.participantIdList()))
				.toList();
		return (int) participants.stream()
				.filter(p -> participantIds.contains(p.id()))
				.count();
	}

	private String getLastStage(UUID tourId, List<TournamentStage> stages) {
		return stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.max(Comparator.comparing(TournamentStage::stageNumber))
				.map(TournamentStage::name)
				.orElseGet(() -> "none");
	}
}
