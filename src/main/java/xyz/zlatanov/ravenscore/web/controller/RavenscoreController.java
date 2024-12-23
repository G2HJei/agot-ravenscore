package xyz.zlatanov.ravenscore.web.controller;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static xyz.zlatanov.ravenscore.web.ControllerConstants.*;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.ravenscore.web.model.export.TournamentExport;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.ImportParticipantsForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.web.security.TournamentId;
import xyz.zlatanov.ravenscore.web.service.*;
import xyz.zlatanov.ravenscore.web.service.builder.tourdetails.RankingMode;

@Controller
@RequiredArgsConstructor
public class RavenscoreController {

	private final TourneysSummaryService		tourneysSummaryService;
	private final TournamentDetailsService		tournamentDetailsService;
	private final TournamentAdminService		tournamentAdminService;
	private final TournamentStageAdminService	tournamentStageAdminService;
	private final PlayerAdminService			playerAdminService;
	private final GameAdminService				gameAdminService;
	private final BackupService					backupService;

	private final ObjectMapper					objectMapper;

	@GetMapping(ROOT)
	String home(Model model) {
		model.addAttribute("tourneysList", tourneysSummaryService.getPublicTourneys());
		return "home";
	}

	@PostMapping(TOURNAMENT)
	String upsertTournament(@ModelAttribute TournamentForm tournamentForm) {
		val tournamentId = tournamentForm.getId() == null
				? tournamentAdminService.createTournament(tournamentForm)
				: tournamentAdminService.updateTournament(tournamentForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(TOURNAMENT_DETAILS)
	String tourneyDetails(@PathVariable(TOURNAMENT_ID) UUID tournamentId,
			@RequestParam(defaultValue = "SCORE") RankingMode rankingMode,
			@RequestParam(required = false) String error,
			@RequestParam(required = false) String message,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, rankingMode));
		model.addAttribute("error", error);
		model.addAttribute("message", message);
		return "tournament";
	}

	@GetMapping(DELETE_TOURNAMENT)
	String deleteTourney(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId) {
		tournamentAdminService.deleteTournament(tournamentId);
		return "redirect:/";
	}

	@PostMapping(UPSERT_STAGE)
	String upsertStage(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @ModelAttribute StageForm stageForm) {
		tournamentStageAdminService.createNewStage(stageForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_STAGE)
	String removeStage(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId) {
		tournamentStageAdminService.removeStage(stageId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(IMPORT_PARTICIPANTS)
	String importParticipants(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId,
			@ModelAttribute ImportParticipantsForm importParticipantsForm) {
		tournamentStageAdminService.importParticipants(importParticipantsForm);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_PLAYER)
	String upsertPlayer(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @ModelAttribute PlayerForm playerForm) {
		playerAdminService.player(playerForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_PLAYER)
	String removePlayer(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId,
			@PathVariable(PLAYER_ID) UUID playerId) {
		playerAdminService.removePlayer(stageId, playerId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(SUBSTITUTE_PLAYER)
	String substitutePlayer(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId,
			@PathVariable(PARTICIPANT_ID) UUID participantId, @PathVariable(SUBSTITUTE_ID) UUID substituteId) {
		playerAdminService.substitution(participantId, substituteId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_GAME)
	String upsertGame(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @ModelAttribute GameForm gameForm) {
		gameAdminService.game(gameForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_GAME)
	String removeGame(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @PathVariable(GAME_ID) UUID gameId) {
		gameAdminService.removeGame(gameId);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(DOWNLOAD_BACKUP)
	@SneakyThrows
	void download(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, HttpServletResponse response) {
		val exportData = backupService.getBackup(tournamentId);
		response.setContentType(APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader(CONTENT_DISPOSITION, "attachment; filename=tournament-backup.json");
		response.getOutputStream().write(objectMapper.writeValueAsBytes(exportData));
		response.getOutputStream().close();
	}

	@PostMapping(value = RESTORE_BACKUP, consumes = "multipart/form-data")
	@SneakyThrows
	String restoreBackup(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId,
			@RequestParam MultipartFile file) {

		final TournamentExport tournamentExport;
		try {
			tournamentExport = objectMapper.readValue(file.getBytes(), TournamentExport.class);
		} catch (Exception e) {
			throw new RavenscoreException("Invalid backup file.", e);
		}
		try {
			backupService.restoreTournament(tournamentId, tournamentExport);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RavenscoreException("Could not restore backup.", e);
		}
		return redirectToTournamentWithMessage(tournamentId, "Backup successfully restored");
	}

}
