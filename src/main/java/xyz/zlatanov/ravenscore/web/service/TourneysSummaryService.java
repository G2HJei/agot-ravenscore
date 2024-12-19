package xyz.zlatanov.ravenscore.web.service;

import static xyz.zlatanov.ravenscore.Utils.DATE_FORMATTER;

import java.util.*;

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
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentSummaryModel;

@Service
@RequiredArgsConstructor
public class TourneysSummaryService {

	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final TournamentStageRepository	tournamentStageRepository;

	@Transactional(readOnly = true)
	public List<TournamentSummaryModel> getPublicTourneys() {
		val tourneys = tournamentRepository.findByHiddenFalseOrderByPinnedDescStartDateDesc();
		val tourIds = tourneys.stream().map(Tournament::id).toList();
		val stages = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(tourIds);
		val participantIds = stages.stream().flatMap(ts -> Arrays.stream(ts.participantIdList())).toList();
		val participants = participantRepository.findByIdInOrderByName(participantIds);
		return buildModel(tourneys, stages, participants);
	}

	private List<TournamentSummaryModel> buildModel(List<Tournament> tourneys, List<TournamentStage> stages,
			List<Participant> participants) {
		return tourneys.stream()
				.map(t -> getTournamentSummaryModel(stages, participants, t))
				.sorted((o1, o2) -> o1.statusDate() == null && o2.statusDate() == null ? 0
						: o1.statusDate() == null ? -1
								: o2.statusDate() == null ? 1
										: o1.statusDate().compareTo(o2.statusDate()))
				.toList();
	}

	private TournamentSummaryModel getTournamentSummaryModel(List<TournamentStage> stages, List<Participant> participants,
			Tournament tour) {
		val lastStage = getLastStage(tour.id(), stages);
		return new TournamentSummaryModel()
				.id(tour.id().toString())
				.name(tour.name())
				.numberOfParticipants(getNumberOfParticipants(tour.id(), stages, participants))
				.statusLabel(getStatusLabel(lastStage))
				.statusDate(getStatusDate(tour, lastStage))
				.pinned(tour.pinned());
	}

	private Integer getNumberOfParticipants(UUID tourId, List<TournamentStage> stages, List<Participant> participants) {
		val participantIds = stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.flatMap(s -> Arrays.stream(s.participantIdList()))
				.toList();
		return (int) participants.stream()
				.filter(p -> participantIds.contains(p.id()))
				.map(Participant::name)
				.distinct()
				.count();
	}

	private Optional<TournamentStage> getLastStage(UUID tourId, List<TournamentStage> stages) {
		return stages.stream()
				.filter(s -> s.tournamentId().equals(tourId))
				.max(Comparator.comparing(TournamentStage::startDate));
	}

	private String getStatusLabel(Optional<TournamentStage> lastStage) {
		return lastStage.map(TournamentStage::name).orElse("");
	}

	private static String getStatusDate(Tournament tour, Optional<TournamentStage> lastStage) {
		// todo last updated date
		return lastStage.map(TournamentStage::startDate).map(DATE_FORMATTER::format)
				.orElseGet(() -> DATE_FORMATTER.format(tour.startDate()));
	}
}
