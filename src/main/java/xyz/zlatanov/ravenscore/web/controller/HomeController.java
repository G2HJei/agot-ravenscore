package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.domain.model.GameType.A_FEAST_FOR_CROWS;
import static xyz.zlatanov.ravenscore.domain.model.House.LANNISTER;
import static xyz.zlatanov.ravenscore.domain.model.House.STARK;
import static xyz.zlatanov.ravenscore.domain.model.Ruleset.POSITION_PLUS_CITIES;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.transaction.Transactional;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.model.*;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;

@Controller
public class HomeController {

	@GetMapping("/")
	@Transactional
	String home() {
		return "home";
	}

	// todo remove below this
	// vvvvvvvvvvvvvvvvvvvvvv
	@Autowired
	private TournamentRepository tournamentRepository;

	// @PostConstruct
	public void injectTestData() {
		val pesho = new Participant()
				.name("Pesho")
				.profileLink("https://swordsandravens.net/user/totally-real-user");
		val gosho = new Participant()
				.name("Gosho")
				.profileLink("https://swordsandravens.net/user/totally-real-user");
		val substitute = new Participant()
				.name("Substitute")
				.substitute(true)
				.profileLink("https://swordsandravens.net/user/totally-real-user");

		val game = new Game()
				.name("Test Game 1")
				.gameType(A_FEAST_FOR_CROWS)
				.gameLink("https://swordsandravens.net/play/totally-real-game")
				.currentRound(2)
				.playerList(List.of(
						new Player().house(LANNISTER),
						new Player().house(STARK)));

		game.addParticipants(pesho, gosho);

		val tour = new Tournament()
				.name("My test tourney")
				.description("None whatsoever")
				.scoring(POSITION_PLUS_CITIES)
				.tournamentKey("test")
				.tournamentStageList(List.of(
						new TournamentStage()
								.name("Round 1")
								.participantList(List.of(pesho, gosho))
								.gameList(List.of(game))))
				.participantList(List.of(pesho, gosho, substitute));
		tournamentRepository.save(tour);
	}
}
