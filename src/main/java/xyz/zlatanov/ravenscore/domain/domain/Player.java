package xyz.zlatanov.ravenscore.domain.domain;

import static jakarta.persistence.EnumType.STRING;

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
public class Player {

	@Id
	@UuidGenerator
	private UUID	id;

	@Enumerated(STRING)
	private House	house;

	private Integer	rank;

	private Integer	castles;

	private Integer	score;

	private Integer	penaltyPoints	= 0;

	private UUID	gameId;

	private UUID	participantId;
}
