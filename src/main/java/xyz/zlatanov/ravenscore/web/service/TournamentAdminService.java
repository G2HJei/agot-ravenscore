package xyz.zlatanov.ravenscore.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentStageRepository;
import xyz.zlatanov.ravenscore.web.model.tourdetails.StageForm;

@Service
@RequiredArgsConstructor
public class TournamentAdminService {

	private final TournamentRepository		tournamentRepository;
	private final TournamentStageRepository	tournamentStageRepository;

	@Transactional
	public void createNewStage(String tournamentKeyHash, @Valid StageForm stageForm) {
		val tournament = tournamentRepository.findById(stageForm.getTournamentId())
				.orElseThrow(() -> new RavenscoreException("Tournament not found."));
		if (!tournament.validateUnlockHash(tournamentKeyHash)) {
			throw new RavenscoreException("Tournament administration locked.");
		}
		tournamentStageRepository.save(new TournamentStage()
				.name(stageForm.getName())
				.tournamentId(stageForm.getTournamentId())
				.qualificationCount(stageForm.getQualificationCount()));
	}
}
