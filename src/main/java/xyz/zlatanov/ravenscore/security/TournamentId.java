package xyz.zlatanov.ravenscore.security;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = TournamentIdValidator.class)
@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
public @interface TournamentId {

	String message() default "Invalid tournament id";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
