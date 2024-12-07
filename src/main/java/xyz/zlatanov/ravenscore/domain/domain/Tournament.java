package xyz.zlatanov.ravenscore.domain.domain;

import static jakarta.persistence.EnumType.STRING;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Tournament {

	@Id
	@UuidGenerator
	private UUID		id;

	private String		name;

	private String		description;

	@Enumerated(STRING)
	private Scoring		scoring;

	private boolean		hidden		= false;

	private String		tournamentKey;

	private LocalDate	startDate	= LocalDate.now();
}
