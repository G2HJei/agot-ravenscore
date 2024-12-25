package xyz.zlatanov.ravenscore.domain.domain;

import static jakarta.persistence.EnumType.STRING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Accessors(fluent = true)
@Entity
@Slf4j
public class Tournament {

	@Id
	@UuidGenerator
	private UUID			id;

	private String			name;

	private String			description;

	@Enumerated(STRING)
	private Scoring			scoring;

	private boolean			hidden		= false;

	private String			tournamentKey;

	private LocalDate		startDate	= LocalDate.now();

	private LocalDateTime	lastUpdated;

	private boolean			pinned		= false;

	public boolean validateTournamentKey(String keyToValidate) {
		return validateUnlockHash(keyToValidate.hashCode());
	}

	public boolean validateUnlockHash(String checkHash) {
		try {
			return StringUtils.isNotBlank(checkHash) && Objects.equals(tournamentKey.hashCode(), Integer.parseInt(checkHash));
		} catch (NumberFormatException e) {
			log.warn("Unexpected tournament key hash: {}", checkHash);
			return false;
		}
	}

	public boolean validateUnlockHash(Integer checkHash) {
		return tournamentKey.hashCode() == checkHash;
	}
}
