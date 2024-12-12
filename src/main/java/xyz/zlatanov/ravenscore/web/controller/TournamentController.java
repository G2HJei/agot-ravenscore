package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.web.RoutingConstants.*;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;

@Controller
@RequiredArgsConstructor
public class TournamentController {

	private static final String				adminCookieName	= "tournament-key-hash";
	private final TournamentDetailsService	tournamentDetailsService;
	private final TournamentAdminService	tournamentAdminService;

	@GetMapping(TOURNAMENT_DETAILS)
	String tourneyDetails(
			@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName, defaultValue = "") String tournamentKeyHash,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, tournamentKeyHash));
		model.addAttribute("newStageForm", new StageForm().setTournamentId(tournamentId));
		model.addAttribute("newPlayerForm", new PlayerForm().setTournamentId(tournamentId));
		return "tournament";
	}

	@PostMapping(UPSERT_STAGE)
	String upsertStage(@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash,
			@ModelAttribute StageForm stageForm) {
		tournamentAdminService.createNewStage(tournamentKeyHash, stageForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_STAGE)
	String removeStage(@PathVariable UUID tournamentId, @PathVariable UUID stageId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash) {
		tournamentAdminService.removeStage(tournamentKeyHash, tournamentId, stageId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_PLAYER)
	String upsertPlayer(@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash,
			@ModelAttribute PlayerForm playerForm) {
		tournamentAdminService.player(tournamentKeyHash, playerForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_PLAYER)
	String removePlayer(@PathVariable UUID tournamentId, @PathVariable UUID stageId, @PathVariable UUID playerId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash) {
		tournamentAdminService.removePlayer(tournamentKeyHash, tournamentId, stageId, playerId);
		return redirectToTournament(tournamentId);
	}

	@PostMapping(UPSERT_GAME)
	String upsertGame(@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash,
			@ModelAttribute GameForm gameForm) {
		tournamentAdminService.game(tournamentKeyHash, tournamentId, gameForm);
		return redirectToTournament(tournamentId);
	}

	@GetMapping(REMOVE_GAME)
	String removeGame(@PathVariable UUID tournamentId,
			@PathVariable UUID gameId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash) {
		tournamentAdminService.removeGame(tournamentKeyHash, tournamentId, gameId);
		return redirectToTournament(tournamentId);
	}
}
