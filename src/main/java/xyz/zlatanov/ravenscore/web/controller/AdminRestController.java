package xyz.zlatanov.ravenscore.web.controller;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static xyz.zlatanov.ravenscore.web.RoutingConstants.UPDATE_ROUND;
import static xyz.zlatanov.ravenscore.web.controller.RavenscoreController.ADMIN_COOKIE_NAME;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.web.service.TournamentAdminService;

@RestController
@RequiredArgsConstructor
public class AdminRestController {

	private final TournamentRepository		tournamentRepository;
	private final TournamentAdminService	tournamentAdminService;

	@PostMapping(value = "/tournament/{tournamentId}/unlock-tournament", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
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

	private boolean unlockKeyIsValid(UUID tournamentId, String unlockKey) {
		return tournamentRepository.findById(tournamentId)
				.map(t -> t.validateTournamentKey(unlockKey))
				.orElse(false);
	}
}
