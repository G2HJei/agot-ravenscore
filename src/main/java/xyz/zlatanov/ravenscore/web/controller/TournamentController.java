package xyz.zlatanov.ravenscore.web.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;

@Controller
@RequiredArgsConstructor
public class TournamentController {

	private final TournamentDetailsService tournamentDetailsService;

	@GetMapping("/tournament/{tournamentId}")
	String tourneyDetails(
			@PathVariable UUID tournamentId,
			@CookieValue(name = "tournamentKeyHash", defaultValue = "") String tournamentKeyHash,
			Model model) {
		model.addAttribute("model", tournamentDetailsService.getTournamentDetails(tournamentId, tournamentKeyHash));
		return "tournament";
	}
}
