package xyz.zlatanov.ravenscore.web.service;

import java.util.Arrays;
import java.util.UUID;

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
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.ImportParticipantsForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.web.security.TournamentAdminOperation;

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
	public void removeStage(UUID tournamentId, UUID stageId) {
		if (!gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stageId).isEmpty()) {
			throw new RavenscoreException("Cannot delete a stage with existing games.");
		}
		val stage = tournamentStageRepository.findById(stageId)
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		val participantIds = stage.participantIdList();
		tournamentStageRepository.delete(stage);
		deleteOrphanedParticipants(tournamentId, participantIds);
	}

	@Transactional
	@TournamentAdminOperation
	public void importParticipants(@Valid ImportParticipantsForm importParticipantsForm) {
		val stage = tournamentStageRepository.findById(importParticipantsForm.getStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
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

	private void createStage(@Valid StageForm stageForm) {
		tournamentStageRepository.save(new TournamentStage()
				.name(stageForm.getName())
				.tournamentId(stageForm.getTournamentId())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	private void updateStage(@Valid StageForm stageForm) {
		val stage = tournamentStageRepository.findById(stageForm.getStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		tournamentStageRepository.save(stage
				.name(stageForm.getName())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	private void deleteOrphanedParticipants(UUID tournamentId, UUID[] participantIdList) {
		var orphanCandidateIds = Arrays.copyOf(participantIdList, participantIdList.length);
		val otherStageList = tournamentStageRepository.findByTournamentIdOrderByStartDateDesc(tournamentId);
		for (val otherStage : otherStageList) {
			UUID[] osParticipantIds = otherStage.participantIdList();
			orphanCandidateIds = Arrays.stream(orphanCandidateIds)
					.filter(pId -> !Arrays.asList(osParticipantIds).contains(pId))
					.toArray(UUID[]::new);
		}
		participantRepository.deleteAllById(Arrays.asList(orphanCandidateIds));
	}
}