package xyz.zlatanov.ravenscore.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.web.model.tourdetails.UnlockRequest;

@RestController
@RequiredArgsConstructor
public class AdminRestController {

	private final TournamentRepository tournamentRepository;

	@PostMapping(value = "/unlock-tournament", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<String> unlock(@RequestBody final UnlockRequest unlockRequest) {
		return unlockKeyIsValid(unlockRequest)
				? ok(String.valueOf(unlockRequest.tournamentKey().hashCode()))
				: badRequest().build();
	}

	private boolean unlockKeyIsValid(UnlockRequest unlockRequest) {
		val tournament = tournamentRepository.findById(UUID.fromString(unlockRequest.tournamentId())).orElse(null);
		return tournament != null && tournament.validateTournamentKey(unlockRequest.tournamentKey());
	}
}
