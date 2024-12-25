package xyz.zlatanov.ravenscore.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.Utils;
import xyz.zlatanov.ravenscore.domain.domain.Participant;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.GameRepository;
import xyz.zlatanov.ravenscore.domain.repository.ParticipantRepository;
import xyz.zlatanov.ravenscore.domain.repository.SubstituteRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentStageRepository;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.ImportParticipantsForm;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

@Service
@RequiredArgsConstructor
public class TournamentStageAdminService {

	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;

	@Transactional
	@TournamentAdminOperation
	public void createNewStage(@Valid StageForm stageForm) {
		if (stageForm.getStageId() == null) {
			createStage(stageForm);
		} else {
			updateStage(stageForm);
		}
	}

	@Transactional
	@TournamentAdminOperation
	public void removeStage(UUID stageId) {
		if (!gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stageId).isEmpty()) {
			throw new RavenscoreException("Cannot delete a stage with existing games.");
		}
		tournamentStageRepository.deleteAndCleanup(stageId);
	}

	@Transactional
	@TournamentAdminOperation
	public void importParticipants(@Valid ImportParticipantsForm importParticipantsForm) {
		val stage = tournamentStageRepository.findById(importParticipantsForm.getStageId()).orElseThrow();
		val stageParticipants = participantRepository.findByIdInOrderByName(Arrays.asList(stage.participantIdList()));
		val stageParticipantsNames = stageParticipants.stream().map(Participant::name).toList();
		val selectedParticipants = participantRepository
				.findByIdInOrderByName(Arrays.asList(importParticipantsForm.getParticipantIdList()));
		val selectedParticipantsNames = selectedParticipants.stream().map(Participant::name).toList();
		val commonNames = stageParticipantsNames.stream()
				.filter(selectedParticipantsNames::contains)
				.toList();
		if (!commonNames.isEmpty()) {
			throw new RavenscoreException(
					String.format("Players(s) are already in present this tournament stage: %s", String.join(", ", commonNames)));
		}

		// todo track stage complete/updated

		val clonedParticipants = participantRepository.saveAll(selectedParticipants.stream()
				.map(Participant::clone)
				.toList());
		var newStageParticipantIds = stage.participantIdList();
		for (val cloned : clonedParticipants) {
			newStageParticipantIds = Utils.addToArray(cloned.id(), newStageParticipantIds);
		}
		tournamentStageRepository.save(stage.participantIdList(newStageParticipantIds));
	}

	@Transactional
	@TournamentAdminOperation
	public void finalizeRankings(UUID stageId, UUID[] finalizedRankingsArray) {
		// replaced participants will not be present in the finalized rankings so find and add them now
		val finalizedRankings = new ArrayList<>(Arrays.stream(finalizedRankingsArray).toList());
		val stage = tournamentStageRepository.findById(stageId).orElseThrow();
		val replacedParticipants = getReplacedParticipants(stage, finalizedRankings);
		finalizedRankings.addAll(replacedParticipants);
		stage.participantIdList(finalizedRankings.toArray(UUID[]::new));
		stage.completed(true);
		tournamentStageRepository.save(stage);
	}

	private void createStage(@Valid StageForm stageForm) {
		tournamentStageRepository.save(new TournamentStage()
				.name(stageForm.getName())
				.tournamentId(stageForm.getTournamentId())
				.qualificationCount(stageForm.getQualificationCount()))
				.completed(false);
	}

	private void updateStage(@Valid StageForm stageForm) {
		val stage = tournamentStageRepository.findById(stageForm.getStageId()).orElseThrow();
		if (stage.completed() && !Objects.equals(stage.qualificationCount(), stageForm.getQualificationCount())) {
			throw new RavenscoreException("Cannot change the qualifiers count of a completed stage.");
		}
		tournamentStageRepository.save(stage
				.name(stageForm.getName())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	private List<UUID> getReplacedParticipants(TournamentStage stage, List<UUID> finalizedRankings) {
		val replacedParticipants = new ArrayList<>(Arrays.stream(stage.participantIdList()).toList());
		replacedParticipants.removeAll(finalizedRankings);
		return replacedParticipants;
	}
}