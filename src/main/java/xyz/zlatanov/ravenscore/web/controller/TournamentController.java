package xyz.zlatanov.ravenscore.web.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.model.tourdetails.StageForm;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;

@Controller
@RequiredArgsConstructor
public class TournamentController {

	private final TournamentDetailsService	tournamentDetailsService;
	private final TournamentAdminService	tournamentAdminService;

	@GetMapping("/tournament/{tournamentId}")
	String tourneyDetails(
			@PathVariable UUID tournamentId,
			@CookieValue(name = "tournamentKeyHash", defaultValue = "") String tournamentKeyHash,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, tournamentKeyHash));
		model.addAttribute("newStageForm", new StageForm().setTournamentId(tournamentId));
		return "tournament";
	}

	@PostMapping("/tournament/{tournamentId}/new-stage")
	String tourneyDetails(@PathVariable UUID tournamentId,
			@CookieValue(name = "tournamentKeyHash", defaultValue = "") String tournamentKeyHash,
			@ModelAttribute StageForm newStageForm) {
		tournamentAdminService.createNewStage(tournamentKeyHash, newStageForm);
		return "redirect:/tournament/" + tournamentId;
	}
}
