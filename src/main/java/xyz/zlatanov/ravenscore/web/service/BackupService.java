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
import xyz.zlatanov.ravenscore.web.model.export.TournamentExport;
import xyz.zlatanov.ravenscore.web.security.TournamentAdminOperation;
import xyz.zlatanov.ravenscore.web.service.builder.tourexport.TournamentExportBuilder;

@Service
@RequiredArgsConstructor
public class BackupService {

	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional(readOnly = true)
	@TournamentAdminOperation
	public TournamentExport getBackup(UUID tournamentId) {
		val tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		val tourStages = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(List.of(tournament.id()));
		val stageIds = tourStages.stream().map(TournamentStage::id).toList();
		val participantIds = tourStages.stream().flatMap(ts -> Arrays.stream(ts.participantIdList())).toList();
		val participants = participantRepository.findByIdInOrderByName(participantIds);
		val substitutes = substituteRepository.findByTournamentIdInOrderByName(List.of(tournamentId));
		val games = gameRepository.findByTournamentStageIdInOrderByTypeAscNameAsc(stageIds);
		val gameIds = games.stream().map(Game::id).toList();
		val players = playerRepository.findByGameIdInOrderByPointsDesc(gameIds);
		return new TournamentExportBuilder(tournament, tourStages, substitutes,
				participants, games, players).build();
	}
}
