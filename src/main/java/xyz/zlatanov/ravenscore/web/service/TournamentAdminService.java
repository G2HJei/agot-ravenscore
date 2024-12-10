package xyz.zlatanov.ravenscore.web.service;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

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
		tournamentStageRepository.save(new TournamentStage()
				.name(stageForm.getName())
				.tournamentId(stageForm.getTournamentId())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	@Transactional
	public void addPlayer(String tournamentKeyHash, PlayerForm playerForm) {
		validateAdminRights(tournamentKeyHash, playerForm.getTournamentId());
		if (playerForm.getTournamentStageId() != null) {
			createParticipant(playerForm);
		} else {
			createSubstitute(playerForm);
		}
	}

	private void validateAdminRights(String tournamentKeyHash, UUID tournamentId) {
		val tournament = tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new RavenscoreException("Tournament not found."));
		if (!tournament.validateUnlockHash(tournamentKeyHash)) {
			throw new RavenscoreException("Tournament administration locked.");
		}
	}

	private void createSubstitute(PlayerForm playerForm) {
		substituteRepository.save(new Substitute()
				.name(playerForm.getName())
				.tournamentId(playerForm.getTournamentId())
				.profileLinks(playerForm.getProfileLinks()));
	}

	private void createParticipant(PlayerForm playerForm) {
		val participant = participantRepository.save(
				new Participant()
						.name(playerForm.getName())
						.profileLinks(playerForm.getProfileLinks()));
		val stage = tournamentStageRepository.findById(playerForm.getTournamentStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid tournament stage."));
		val participantIdList = new TreeSet<>(List.of(stage.participantIdList()));
		participantIdList.add(participant.id());
		tournamentStageRepository.save(stage.participantIdList(participantIdList.toArray(new UUID[] {})));
	}
}
