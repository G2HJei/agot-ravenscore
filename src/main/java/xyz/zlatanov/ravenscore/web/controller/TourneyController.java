package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.web.service.TourneyService;

@Controller
@RequiredArgsConstructor
public class TourneyController {

	private final TourneyService tourneyService;

	@GetMapping("/tourneys")
	String tourneys() {
		val model = tourneyService.getPublicTourneys();
		return "tourneys";
	}
}
