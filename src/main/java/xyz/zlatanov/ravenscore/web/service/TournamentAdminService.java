package xyz.zlatanov.ravenscore.web.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Tournament;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.web.security.TourInfoService;
import xyz.zlatanov.ravenscore.web.security.TournamentAdminOperation;

@Service
@RequiredArgsConstructor
@Validated
public class TournamentAdminService {

	private final TourInfoService			tourInfoService;
	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional
	public UUID createTournament(@Valid TournamentForm tournamentForm) {
		val tournament = tournamentRepository.save(new Tournament()
				.name(tournamentForm.getName())
				.scoring(tournamentForm.getScoring())
				.description(tournamentForm.getDescription())
				.hidden(tournamentForm.getHidden())
				.tournamentKey(tournamentForm.getTournamentKey()));
		return tournament.id();
	}

	@Transactional
	@TournamentAdminOperation
	public UUID updateTournament(@Valid TournamentForm tournamentForm) {
		val tournament = tournamentRepository.findById(tournamentForm.getId()).orElseThrow();
		tournamentRepository.save(tournament
				.id(tourInfoService.getTournamentId())
				.name(tournamentForm.getName())
				.scoring(tournamentForm.getScoring())
				.description(tournamentForm.getDescription())
				.hidden(tournamentForm.getHidden()));
		return tournamentForm.getId();
	}

	@Transactional
	@TournamentAdminOperation
	public void deleteTournament(UUID tournamentId) {
		tournamentRepository.deleteTournamentData(tournamentId);
		tournamentRepository.deleteById(tournamentId);
	}

}
