package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.service.TourneysSummaryService;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final TourneysSummaryService tourneysSummaryService;

	@GetMapping("/")
	String home(Model model) {
		model.addAttribute("tourneysList", tourneysSummaryService.getPublicTourneys());
		return "home";
	}
}
