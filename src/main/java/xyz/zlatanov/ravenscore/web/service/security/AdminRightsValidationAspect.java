package xyz.zlatanov.ravenscore.web.service.security;

import static xyz.zlatanov.ravenscore.web.ControllerConstants.ADMIN_COOKIE_NAME;
import static xyz.zlatanov.ravenscore.web.ControllerConstants.TOURNAMENT_ID_COOKIE_NAME;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.web.service.RavenscoreException;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminRightsValidationAspect {

	private final TournamentRepository tournamentRepository;

	@Before("@annotation(xyz.zlatanov.ravenscore.web.service.security.TourAdminOperation)")
	public void tournamentIsUnlocked() {
		val request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		val tournamentId = getTournamentId(request);
		val adminCookie = getAdminCookie(request);
		val tournamentIsUnlocked = tournamentRepository.findById(tournamentId)
				.map(t -> validateUnlockHash(t.tournamentKey(), adminCookie))
				.orElse(false);
		if (!tournamentIsUnlocked) {
			throw new RavenscoreException("Tournament administration locked.");
		}
	}

	private UUID getTournamentId(HttpServletRequest request) {
		return Optional.ofNullable(request.getCookies())
				.flatMap(cookies -> Arrays.stream(cookies)
						.filter(c -> TOURNAMENT_ID_COOKIE_NAME.equals(c.getName()))
						.map(Cookie::getValue)
						.map(UUID::fromString)
						.findFirst())
				.orElse(null);
	}

	private Integer getAdminCookie(HttpServletRequest request) {
		return Optional.ofNullable(request.getCookies())
				.flatMap(cookies -> Arrays.stream(cookies)
						.filter(c -> ADMIN_COOKIE_NAME.equals(c.getName()))
						.map(Cookie::getValue)
						.map(Integer::valueOf)
						.findFirst())
				.orElse(null);
	}

	public boolean validateUnlockHash(String tournamentKey, Integer checkHash) {
		return tournamentKey.hashCode() == checkHash;
	}

}
