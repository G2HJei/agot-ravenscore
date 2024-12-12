package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.web.RoutingConstants.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;
import xyz.zlatanov.ravenscore.web.model.toursummary.NewTournamentForm;
import xyz.zlatanov.ravenscore.web.service.NewTournamentService;
import xyz.zlatanov.ravenscore.web.service.TourneysSummaryService;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final TourneysSummaryService	tourneysSummaryService;
	private final NewTournamentService		newTournamentService;

	@GetMapping(ROOT)
	String home(Model model) {
		model.addAttribute("scoringOptions", Scoring.values());
		model.addAttribute("newTournamentForm", new NewTournamentForm());
		model.addAttribute("tourneysList", tourneysSummaryService.getPublicTourneys());
		return "home";
	}

	@PostMapping(NEW_TOURNAMENT)
	String newTournament(@ModelAttribute NewTournamentForm form) {
		return redirectToTournament(newTournamentService.newTournament(form));
	}
}
