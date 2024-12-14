package xyz.zlatanov.ravenscore.web.controller;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static xyz.zlatanov.ravenscore.web.RoutingConstants.*;
import static xyz.zlatanov.ravenscore.web.controller.RavenscoreController.ADMIN_COOKIE_NAME;

import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.RankingsForm;
import xyz.zlatanov.ravenscore.web.service.RavenscoreException;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminRestController {

	private final TournamentRepository		tournamentRepository;
	private final TournamentAdminService	tournamentAdminService;

	@PostMapping(value = UNLOCK_TOURNAMENT, consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<String> unlock(@PathVariable UUID tournamentId, @RequestBody final String tournamentKey) {
		return unlockKeyIsValid(tournamentId, tournamentKey)
				? ok(String.valueOf(tournamentKey.hashCode()))
				: badRequest().build();
	}

	@GetMapping(value = UPDATE_ROUND)
	public ResponseEntity<Void> updateRound(@PathVariable UUID tournamentId, @PathVariable UUID gameId, @PathVariable Integer round,
			@CookieValue(name = ADMIN_COOKIE_NAME) String tournamentKeyHash) {
		tournamentAdminService.updateRound(tournamentKeyHash, tournamentId, gameId, round);
		return noContent().build();
	}

	@PostMapping(value = UPDATE_GAME_RANKINGS, produces = TEXT_PLAIN_VALUE)
	public ResponseEntity<String> updateRankings(@PathVariable UUID tournamentId,
			@RequestBody RankingsForm rankingsForm,
			@CookieValue(name = ADMIN_COOKIE_NAME) String tournamentKeyHash) {
		try {
			tournamentAdminService.updateRankings(tournamentKeyHash, tournamentId, rankingsForm);
		} catch (RavenscoreException e) {
			return badRequest().body(e.getMessage());
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			return internalServerError().body("Unexpected error occurred.");
		}
		return noContent().build();
	}

	private boolean unlockKeyIsValid(UUID tournamentId, String unlockKey) {
		return tournamentRepository.findById(tournamentId)
				.map(t -> t.validateTournamentKey(unlockKey))
				.orElse(false);
	}
}
