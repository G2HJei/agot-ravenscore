package xyz.zlatanov.ravenscore.security;

import static xyz.zlatanov.ravenscore.controller.ControllerConstants.ADMIN_COOKIE_NAME;
import static xyz.zlatanov.ravenscore.controller.ControllerConstants.TOURNAMENT_ID_COOKIE_NAME;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.service.RavenscoreException;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TourInfoService {

	public static final String			TOURNAMENT_ADMIN_OPERATION_POINTCUT	= "@annotation(xyz.zlatanov.ravenscore.security.TournamentAdminOperation)";
	private final TournamentRepository	tournamentRepository;

	// AOP ensures the selected tournament is unlocked while performing an administrative operation
	@Before(TOURNAMENT_ADMIN_OPERATION_POINTCUT)
	public void tournamentIsUnlocked() {
		val tournamentIsUnlocked = tournamentRepository.findById(getTournamentId())
				.map(t -> validateUnlockHash(t.tournamentKey(), getAdminCookie()))
				.orElse(false);
		if (!tournamentIsUnlocked) {
			throw new RavenscoreException("Tournament administration locked.");
		}
	}

	public boolean tournamentIsUnlocked(UUID tournamentId) {
		val tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		return validateUnlockHash(tournament.tournamentKey(), getAdminCookie());
	}

	// retrieve the tournament id based on the selection to ensure no cross-unlocking and HTML manipulation is possible
	public UUID getTournamentId() {
		val request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		return Optional.ofNullable(request.getCookies())
				.flatMap(cookies -> Arrays.stream(cookies)
						// debug
						.peek(c -> log.info("cookie: {} = {}", c.getName(), c.getValue()))
						.filter(c -> TOURNAMENT_ID_COOKIE_NAME.equals(c.getName()))
						.map(Cookie::getValue)
						.map(UUID::fromString)
						.findFirst())
				.orElse(null);
	}

	private Integer getAdminCookie() {
		val request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		return Optional.ofNullable(request.getCookies())
				.flatMap(cookies -> Arrays.stream(cookies)
						.filter(c -> ADMIN_COOKIE_NAME.equals(c.getName()))
						.map(Cookie::getValue)
						.map(Integer::valueOf)
						.findFirst())
				.orElse(null);
	}

	public boolean validateUnlockHash(String tournamentKey, Integer checkHash) {
		return checkHash != null && tournamentKey.hashCode() == checkHash;
	}

}
