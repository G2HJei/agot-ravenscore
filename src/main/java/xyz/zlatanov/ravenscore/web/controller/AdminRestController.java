package xyz.zlatanov.ravenscore.web.controller;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;

@RestController
@RequiredArgsConstructor
public class AdminRestController {

	private final TournamentRepository tournamentRepository;

	@PostMapping(value = "/tournament/{tournamentId}/unlock-tournament", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<String> unlock(@PathVariable UUID tournamentId, @RequestBody final String tournamentKey) {
		return unlockKeyIsValid(tournamentId, tournamentKey)
				? ok(String.valueOf(tournamentKey.hashCode()))
				: badRequest().build();
	}

	private boolean unlockKeyIsValid(UUID tournamentId, String unlockKey) {
		return tournamentRepository.findById(tournamentId)
				.map(t -> t.validateTournamentKey(unlockKey))
				.orElse(false);
	}
}
