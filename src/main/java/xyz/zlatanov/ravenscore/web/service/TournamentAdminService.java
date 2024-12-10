package xyz.zlatanov.ravenscore.web.service;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Participant;
import xyz.zlatanov.ravenscore.domain.domain.Substitute;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.ParticipantRepository;
import xyz.zlatanov.ravenscore.domain.repository.SubstituteRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentStageRepository;
import xyz.zlatanov.ravenscore.web.model.tourdetails.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.StageForm;

@Service
@RequiredArgsConstructor
public class TournamentAdminService {

	private final TournamentRepository		tournamentRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final SubstituteRepository		substituteRepository;
	private final ParticipantRepository		participantRepository;

	@Transactional
	public void createNewStage(String tournamentKeyHash, @Valid StageForm stageForm) {
		validateAdminRights(tournamentKeyHash, stageForm.getTournamentId());
		if (stageForm.getStageId() == null) {
			createStage(stageForm);
		} else {
			updateStage(stageForm);
		}
	}

	@Transactional
	public void player(String tournamentKeyHash, @Valid PlayerForm playerForm) {
		validateAdminRights(tournamentKeyHash, playerForm.getTournamentId());
		if (playerForm.getTournamentStageId() != null) {
			participant(playerForm);
		} else {
			substitute(playerForm);
		}
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

	private void validateAdminRights(String tournamentKeyHash, UUID tournamentId) {
		val tournament = tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new RavenscoreException("Tournament not found."));
		if (!tournament.validateUnlockHash(tournamentKeyHash)) {
			throw new RavenscoreException("Tournament administration locked.");
		}
	}

	private void substitute(PlayerForm playerForm) {
		if (playerForm.getPlayerId() == null) {
			createSubstitute(playerForm);
		} else {
			updateSubstitute(playerForm);
		}
	}

	private void createSubstitute(PlayerForm playerForm) {
		substituteRepository.save(new Substitute()
				.name(playerForm.getName())
				.tournamentId(playerForm.getTournamentId())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void updateSubstitute(PlayerForm playerForm) {
		val substitute = substituteRepository.findById(playerForm.getPlayerId())
				.orElseThrow(() -> new RavenscoreException("Invalid substitute"));
		substituteRepository.save(substitute
				.name(playerForm.getName())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void participant(PlayerForm playerForm) {
		if (playerForm.getPlayerId() == null) {
			createParticipant(playerForm);
		} else {
			updateParticipant(playerForm);
		}
	}

	private void createParticipant(PlayerForm playerForm) {
		val participant = participantRepository.save(
				new Participant()
						.name(playerForm.getName())
						.profileLinks(trimEmpty(playerForm.getProfileLinks())));
		val stage = tournamentStageRepository.findById(playerForm.getTournamentStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid tournament stage."));
		val participantIdList = new TreeSet<>(List.of(stage.participantIdList()));
		participantIdList.add(participant.id());
		tournamentStageRepository.save(stage.participantIdList(participantIdList.toArray(new UUID[] {})));
	}

	private void updateParticipant(PlayerForm playerForm) {
		val participant = participantRepository.findById(playerForm.getPlayerId())
				.orElseThrow(() -> new RavenscoreException("Invalid participant"));
		participantRepository.save(participant
				.name(playerForm.getName())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private String[] trimEmpty(String[] items) {
		val trimmed = Arrays.stream(items)
				.filter(StringUtils::isNotEmpty)
				.distinct()
				.toList();
		return trimmed.toArray(new String[] {});
	}
}
