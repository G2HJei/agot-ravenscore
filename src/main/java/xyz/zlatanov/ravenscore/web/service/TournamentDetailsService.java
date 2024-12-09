package xyz.zlatanov.ravenscore.web.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Game;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.TournamentDetailsBuilder;
import xyz.zlatanov.ravenscore.web.model.tourdetails.TournamentDetailsModel;

@Service
@RequiredArgsConstructor
public class TournamentDetailsService {

	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional(readOnly = true)
	public TournamentDetailsModel getTournamentDetails(UUID tourId, String adminKeyHash) {
		val tournament = tournamentRepository.findById(tourId).orElseThrow(() -> new RavenscoreException("Tournament not found."));
		val tourStages = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(List.of(tournament.id()));
		val stageIds = tourStages.stream().map(TournamentStage::id).toList();
		val participantIds = tourStages.stream().flatMap(ts -> Arrays.stream(ts.participantIdList())).toList();
		val participants = participantRepository.findByIdInOrderByName(participantIds);
		val substitutes = substituteRepository.findByTournamentIdInOrderByName(List.of(tourId));
		val games = gameRepository.findByTournamentStageIdInOrderByTypeAscNameAsc(stageIds);
		val gameIds = games.stream().map(Game::id).toList();
		val players = playerRepository.findByGameIdInOrderByRankAscHouseAsc(gameIds);
		return new TournamentDetailsBuilder(tournament, adminKeyHash, tourStages, substitutes, participants, games, players).build();
	}

}
