package xyz.zlatanov.ravenscore.service;

import static xyz.zlatanov.ravenscore.security.TourInfoService.TOURNAMENT_ADMIN_OPERATION_POINTCUT;

import java.time.LocalDateTime;
import java.util.UUID;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Tournament;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.security.TourInfoService;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

@Aspect
@Service
@RequiredArgsConstructor
@Validated
public class TournamentAdminService {

	private final TourInfoService		tourInfoService;
	private final TournamentRepository	tournamentRepository;

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

	@After(TOURNAMENT_ADMIN_OPERATION_POINTCUT)
	@Transactional
	public void updateLastUpdated() {
		val tourId = tourInfoService.getTournamentId();
		val tournament = tournamentRepository.findById(tourId).orElseThrow();
		tournamentRepository.save(tournament.lastUpdated(LocalDateTime.now()));
	}

}
