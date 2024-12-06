package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.web.service.TourneyService;

@Controller
@RequiredArgsConstructor
public class TourneyController {

	private final TourneyService tourneyService;

	@GetMapping("/tourneys")
	String tourneys() {
		tourneyService.getPublicTourneys();
		return "tourneys";
	}
}
