package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.transaction.Transactional;

@Controller
public class HomeController {

	@GetMapping("/")
	@Transactional
	String home() {
		return "home";
	}
}
