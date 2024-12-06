package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.EnumType.STRING;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
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
	private UUID	id;

	@Column(nullable = false)
	private String	name;

	@Column(length = 2000)
	private String	description;

	@Enumerated(STRING)
	@Column(length = 32, nullable = false)
	private Ruleset	scoring;

	@Column(nullable = false)
	private boolean	hidden	= false;

	@Column(length = 32, nullable = false)
	private String	tournamentKey;

}
