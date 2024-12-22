package xyz.zlatanov.ravenscore.web.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.export.*;
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

	@Transactional
	@TournamentAdminOperation
	public void restoreTournament(UUID tournamentId, TournamentExport tournamentExport) {
		restorePinned(tournamentId, tournamentExport);
		tournamentRepository.deleteTournamentData(tournamentId);
		restoreTournamentData(tournamentId, tournamentExport);
	}

	private void restorePinned(UUID tournamentId, TournamentExport tournamentExport) {
		tournamentExport.pinned(tournamentRepository.findById(tournamentId).orElseThrow().pinned());
	}

	private void restoreTournamentData(UUID tournamentId, TournamentExport tournamentExport) {
		createTournament(tournamentId, tournamentExport);
		createSubstitutes(tournamentId, tournamentExport);
		val participants = createParticipants(tournamentExport);
		tournamentExport.tournamentStageExportList()
				.forEach(tournamentStageExport -> {
					val stageId = createStage(tournamentId, participants, tournamentStageExport);
					tournamentStageExport.gameExportList()
							.forEach(g -> createGame(g, participants, stageId));
				});
	}

	private void createTournament(UUID tournamentId, TournamentExport tournamentExport) {
		val tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		tournamentRepository.save(tournament
				.id(tournamentId)
				.name(tournamentExport.name())
				.description(tournamentExport.description())
				.scoring(tournamentExport.scoring())
				.hidden(tournamentExport.hidden())
				.tournamentKey(tournamentExport.tournamentKey())
				.startDate(tournamentExport.startDate())
				.pinned(tournamentExport.pinned()));
	}

	private void createSubstitutes(UUID tournamentId, TournamentExport tournamentExport) {
		substituteRepository.saveAll(tournamentExport.substituteExportList().stream()
				.map(s -> new Substitute()
						.name(s.name())
						.profileLinks(s.profileLinks())
						.tournamentId(tournamentId))
				.toList());
	}

	private UUID createStage(UUID tournamentId, List<Participant> participants,
			TournamentStageExport tournamentStageExport) {
		val stage = new TournamentStage()
				.tournamentId(tournamentId)
				.name(tournamentStageExport.name())
				.qualificationCount(tournamentStageExport.qualificationCount())
				.startDate(tournamentStageExport.startDate())
				.participantIdList(tournamentStageExport.participantExportList().stream()
						.map(participantExport -> participants.stream()
								.filter(participant -> participant.name().equals(participantExport.name()))
								.findFirst()
								.orElseThrow()
								.id())
						.toArray(UUID[]::new));
		tournamentStageRepository.save(stage);
		return stage.id();
	}

	private void createGame(GameExport g, List<Participant> participants, UUID stageId) {
		val game = new Game()
				.name(g.name())
				.type(g.type())
				.link(g.link())
				.round(g.round())
				.tournamentStageId(stageId)
				.completed(g.completed())
				.participantIdList(participants.stream()
						.filter(p -> g.participantNameList().contains(p.name()))
						.map(Participant::id)
						.toArray(UUID[]::new));
		gameRepository.save(game);
		val players = g.playerExportList().stream()
				.map(pl -> createPlayer(participants, pl, game))
				.toList();
		playerRepository.saveAll(players);
	}

	private List<Participant> createParticipants(TournamentExport tournamentExport) {
		val participants = participantRepository.saveAll(tournamentExport.tournamentStageExportList().stream()
				.flatMap(s -> s.participantExportList().stream())
				.distinct()
				.map(p -> new Participant()
						.name(p.name())
						.profileLinks(p.profileLinks()))
				.toList());
		tournamentExport.tournamentStageExportList().stream()
				.flatMap(s -> s.participantExportList().stream())
				.distinct()
				.filter(pe -> pe.replacementParticipantName() != null)
				.forEach(pe -> setParticipantId(pe, participants));
		participantRepository.saveAll(participants);
		return participants;
	}

	private static void setParticipantId(ParticipantExport participantExport, List<Participant> participants) {
		val participant = participants.stream()
				.filter(p -> p.name().equals(participantExport.name()))
				.findFirst()
				.orElseThrow();
		val replacement = participants.stream()
				.filter(p -> p.name().equals(participantExport.replacementParticipantName()))
				.findFirst()
				.orElseThrow();
		participant.replacementParticipantId(replacement.id());
	}

	private static Player createPlayer(List<Participant> participants, PlayerExport pl, Game game) {
		return new Player()
				.house(pl.house())
				.castles(pl.castles())
				.points(pl.points())
				.penaltyPoints(pl.penaltyPoints())
				.gameId(game.id())
				.participantId(pl.participantName() == null ? null
						: participants.stream()
								.filter(p -> p.name().equals(pl.participantName()))
								.findFirst()
								.orElseThrow()
								.id());
	}

}