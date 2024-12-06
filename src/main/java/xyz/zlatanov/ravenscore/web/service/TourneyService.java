package xyz.zlatanov.ravenscore.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.repository.*;

@Service
@RequiredArgsConstructor
public class TourneyService {

	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional(readOnly = true)
	public void getPublicTourneys() {
		val tourneys = tournamentRepository.findByHiddenFalse();
		System.out.println();
	}
}
