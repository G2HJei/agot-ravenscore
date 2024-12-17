package xyz.zlatanov.ravenscore.web.controller;

import static xyz.zlatanov.ravenscore.web.ControllerConstants.redirectToTournament;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.zlatanov.ravenscore.web.security.TourInfoService;
import xyz.zlatanov.ravenscore.web.service.RavenscoreException;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ErrorHandlerController {

	private final TourInfoService tourInfoService;

	@ExceptionHandler(Exception.class)
	public String unknownException(Exception e) {
		if (e instanceof RavenscoreException) {
			return redirectToTournament(tourInfoService.getTournamentId(), e.getMessage());
		}
		log.error(ExceptionUtils.getStackTrace(e));
		return "error";
	}
}
