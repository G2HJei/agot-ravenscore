package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.web.ControllerConstants.*;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;
import xyz.zlatanov.ravenscore.web.service.TourneysSummaryService;

@Controller
@RequiredArgsConstructor
public class RavenscoreController {

	private final TourneysSummaryService	tourneysSummaryService;
	private final TournamentDetailsService	tournamentDetailsService;
	private final TournamentAdminService	tournamentAdminService;

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
	String tourneyDetails(
			@PathVariable(TOURNAMENT_ID) UUID tournamentId,
			@RequestParam(required = false) String error,
			@CookieValue(name = ADMIN_COOKIE_NAME, defaultValue = "") String tournamentKeyHash,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, tournamentKeyHash));
		model.addAttribute("error", error);
		return "tournament";
	}

	@PostMapping(UPSERT_STAGE)
	String upsertStage(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @ModelAttribute StageForm stageForm) {
		tournamentAdminService.createNewStage(stageForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_STAGE)
	String removeStage(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId) {
		tournamentAdminService.removeStage(tournamentId, stageId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(IMPORT_PARTICIPANTS)
	String importParticipants(@PathVariable(TOURNAMENT_ID) UUID tournamentId) {
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_PLAYER)
	String upsertPlayer(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @ModelAttribute PlayerForm playerForm) {
		tournamentAdminService.player(playerForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_PLAYER)
	String removePlayer(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId,
			@PathVariable(PLAYER_ID) UUID playerId) {
		tournamentAdminService.removePlayer(tournamentId, stageId, playerId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(SUBSTITUTE_PLAYER)
	String substitutePlayer(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @PathVariable(STAGE_ID) UUID stageId,
			@PathVariable(PARTICIPANT_ID) UUID participantId, @PathVariable(SUBSTITUTE_ID) UUID substituteId) {
		tournamentAdminService.substitution(tournamentId, stageId, participantId, substituteId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_GAME)
	String upsertGame(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @ModelAttribute GameForm gameForm) {
		tournamentAdminService.game(tournamentId, gameForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_GAME)
	String removeGame(@PathVariable(TOURNAMENT_ID) UUID tournamentId, @PathVariable(GAME_ID) UUID gameId) {
		tournamentAdminService.removeGame(tournamentId, gameId);
		return redirectToTournament(tournamentId);
	}

}
