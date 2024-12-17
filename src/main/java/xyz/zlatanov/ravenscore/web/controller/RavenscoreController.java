package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.web.ControllerConstants.*;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.ImportParticipantsForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.web.security.TournamentId;
import xyz.zlatanov.ravenscore.web.service.*;

@Controller
@RequiredArgsConstructor
public class RavenscoreController {

	private final TourneysSummaryService		tourneysSummaryService;
	private final TournamentDetailsService		tournamentDetailsService;
	private final TournamentAdminService		tournamentAdminService;
	private final TournamentStageAdminService	tournamentStageAdminService;
	private final PlayerAdminService			playerAdminService;
	private final GameAdminService				gameAdminService;

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
			@RequestParam(required = false) String error, Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId));
		model.addAttribute("error", error);
		return "tournament";
	}

	@PostMapping(UPSERT_STAGE)
	String upsertStage(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @ModelAttribute StageForm stageForm) {
		tournamentStageAdminService.createNewStage(stageForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_STAGE)
	String removeStage(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId) {
		tournamentStageAdminService.removeStage(tournamentId, stageId);
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
	String substitutePlayer(@PathVariable(TOURNAMENT_ID) @TournamentId UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId,
			@PathVariable(PARTICIPANT_ID) UUID participantId, @PathVariable(SUBSTITUTE_ID) UUID substituteId) {
		playerAdminService.substitution(stageId, participantId, substituteId);
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

}
