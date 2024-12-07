package xyz.zlatanov.ravenscore.web.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TourneyController {

	@GetMapping("/tourney/{tourneyId}")
	String tourneyDetails(@PathVariable UUID tourneyId) {
		return "tournament";
	}
}
