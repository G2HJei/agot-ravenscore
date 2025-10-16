package xyz.zlatanov.ravenscore.service;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.model.export.*;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;
import xyz.zlatanov.ravenscore.service.builder.tourexport.TournamentExportBuilder;

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
		tournamentRepository.deleteTournamentData(tournamentId);
		restoreTournamentData(tournamentId, tournamentExport);
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
				.startDate(tournamentExport.startDate()));
	}

	private void createSubstitutes(UUID tournamentId, TournamentExport tournamentExport) {
		substituteRepository.saveAll(tournamentExport.substituteExportList().stream()
				.map(s -> new Substitute()
						.name(s.name())
						.profileLinks(s.profileLinks())
						.tournamentId(tournamentId))
				.toList());
	}

	private UUID createStage(UUID tournamentId, List<Participant> participants, TournamentStageExport stageExport) {
		return tournamentStageRepository.save(new TournamentStage()
				.tournamentId(tournamentId)
				.name(stageExport.name())
				.qualificationCount(stageExport.qualificationCount())
				.startDate(stageExport.startDate())
				.completed(stageExport.completed())
				.participantIdList(buildStageParticipantIdList(participants, stageExport))).id();
	}

	private static UUID[] buildStageParticipantIdList(List<Participant> participants, TournamentStageExport tournamentStageExport) {
		var participantIdList = new HashSet<UUID>();
		for (ParticipantExport participantExport : tournamentStageExport.participantExportList()) {
			val participant = participants.stream()
					.filter(p -> p.name().equals(participantExport.name()))
					.findFirst()
					.orElseThrow();
			participantIdList.add(participant.id());
			if (participant.replacementParticipantId() != null) {
				participantIdList.add(participant.replacementParticipantId());
			}
		}
		return participantIdList.toArray(new UUID[] {});
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
						.map(p -> ofNullable(p.replacementParticipantId()).orElseGet(p::id))
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
				.participantId(ofNullable(pl.participantName())
						.map(name -> participants.stream()
								.filter(p -> p.name().equals(name))
								.findFirst()
								.orElseThrow())
						.map(p -> ofNullable(p.replacementParticipantId())
								.orElse(p.id()))
						.orElse(null));
	}

}