package xyz.zlatanov.ravenscore.web.service.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TournamentIdValidator implements ConstraintValidator<TournamentId, UUID> {

	private final TourInfoService tourInfoService;

	@Override
	public boolean isValid(UUID value, ConstraintValidatorContext context) {
		return Optional.ofNullable(value)
				.map(v -> v.equals(tourInfoService.getTournamentId()))
				.orElse(true);
	}
}
