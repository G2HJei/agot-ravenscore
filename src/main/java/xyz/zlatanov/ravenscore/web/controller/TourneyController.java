package xyz.zlatanov.ravenscore.web.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.web.service.TournamentDetailsService;

@Controller
@RequiredArgsConstructor
public class TourneyController {

	private final TournamentDetailsService tournamentDetailsService;

	@GetMapping("/tourney/{tourneyId}")
	String tourneyDetails(@PathVariable UUID tourneyId) {
		val model = tournamentDetailsService.getTournamentDetails(tourneyId);
		return "tournament";
	}
}
