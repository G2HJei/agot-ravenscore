package xyz.zlatanov.ravenscore.domain.domain;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Substitute {

	@Id
	@UuidGenerator
	private UUID	id;

	@Column(length = 64, nullable = false)
	private String	name;

	@Column(nullable = false)
	private String	profileLink;

	private UUID	tournamentId;
}
