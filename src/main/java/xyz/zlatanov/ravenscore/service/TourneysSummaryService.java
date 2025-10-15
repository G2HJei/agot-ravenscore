package xyz.zlatanov.ravenscore.service;

import static xyz.zlatanov.ravenscore.Utils.DATE_FORMATTER;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Participant;
import xyz.zlatanov.ravenscore.domain.domain.Tournament;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.ParticipantRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentStageRepository;
import xyz.zlatanov.ravenscore.model.toursummary.TournamentSummaryModel;

@Service
@RequiredArgsConstructor
public class TourneysSummaryService {

	private final TournamentRepository		tournamentRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final ParticipantRepository		participantRepository;

	@Transactional(readOnly = true)
	public List<TournamentSummaryModel> getPublicTourneys() {
		val tourneys = tournamentRepository.findByHiddenFalseOrderByPinnedDescLastUpdatedDesc();
		val tourIds = tourneys.stream().map(Tournament::id).toList();
		val stages = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(tourIds);
		return tourneys.stream()
				.map(t -> getTournamentSummaryModel(stages, t))
				.toList();
	}

	private TournamentSummaryModel getTournamentSummaryModel(List<TournamentStage> stages, Tournament tour) {
		return new TournamentSummaryModel()
				.id(tour.id().toString())
				.name(tour.name())
				.numberOfParticipants(getNumberOfParticipants(tour.id(), stages))
				.statusLabel(getStatusLabel(tour.id(), stages))
				.statusDate(DATE_FORMATTER.format(tour.lastUpdated()))
				.winner(getWinner(tour.id(), stages))
				.pinned(tour.pinned());
	}

	private Long getNumberOfParticipants(UUID tourId, List<TournamentStage> stages) {
		return stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.flatMap(s -> Arrays.stream(s.participantIdList()))
				.distinct()
				.count();
	}

	private String getStatusLabel(UUID tourId, List<TournamentStage> stages) {
		return stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.max(Comparator.comparing(TournamentStage::startDate))
				.map(TournamentStage::name).orElse("");
	}

	private String getWinner(UUID tourId, List<TournamentStage> stages) {
		val tourStages = stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.toList();
		if (tourStages.isEmpty() || tourStages.stream().anyMatch(s -> !s.completed())) {
			return null;
		}
		val lastStage = tourStages.getFirst();
		if (lastStage.qualificationCount() != 1) {
			return null;
		}
		val winnerId = lastStage.participantIdList()[0];
		return participantRepository.findById(winnerId)
				.map(Participant::name)
				.orElse(null);
	}
}
