package xyz.zlatanov.ravenscore.web.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.model.tourdetails.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.StageForm;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;

@Controller
@RequiredArgsConstructor
public class TournamentController {

	private static final String				adminCookieName	= "tournament-key-hash";
	private final TournamentDetailsService	tournamentDetailsService;
	private final TournamentAdminService	tournamentAdminService;

	@GetMapping("/tournament/{tournamentId}")
	String tourneyDetails(
			@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName, defaultValue = "") String tournamentKeyHash,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, tournamentKeyHash));
		model.addAttribute("newStageForm", new StageForm().setTournamentId(tournamentId));
		model.addAttribute("newPlayerForm", new PlayerForm().setTournamentId(tournamentId));
		return "tournament";
	}

	@PostMapping("/tournament/{tournamentId}/stage")
	String stage(@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash,
			@ModelAttribute StageForm stageForm) {
		tournamentAdminService.createNewStage(tournamentKeyHash, stageForm);
		return "redirect:/tournament/" + tournamentId;
	}

	@PostMapping("/tournament/{tournamentId}/player")
	String player(@PathVariable UUID tournamentId,
			@CookieValue(name = adminCookieName) String tournamentKeyHash,
			@ModelAttribute PlayerForm playerForm) {
		tournamentAdminService.player(tournamentKeyHash, playerForm);
		return "redirect:/tournament/" + tournamentId;
	}
}
