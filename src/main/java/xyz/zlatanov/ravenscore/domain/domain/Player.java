package xyz.zlatanov.ravenscore.domain.domain;

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
public class Player {

	@Id
	@UuidGenerator
	private UUID	id;

	@Enumerated(STRING)
	@Column(length = 16, nullable = false)
	private House	house;

	private Integer	rank;

	private Integer	castles;

	private Integer	landAreas;

	private Integer	supply;

	private Integer	ironThrone;

	private Integer	score;

	@Column(nullable = false)
	private short	penaltyPoints	= 0;

	@Column(nullable = false)
	private UUID	gameId;

	private UUID	participantId;
}
