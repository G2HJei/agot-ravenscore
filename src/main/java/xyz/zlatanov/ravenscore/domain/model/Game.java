package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.EnumType.STRING;

import java.util.UUID;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Game {

	@Id
	@UuidGenerator
	private UUID		id;

	@Column(nullable = false)
	private String		name;

	@Enumerated(STRING)
	@Column(length = 32, nullable = false)
	private GameType	gameType;

	@Column(nullable = false)
	private String		gameLink;

	private int			round;

	@Column(nullable = false)
	private UUID		tournamentStageId;

	@NotEmpty
	@Type(UUIDArrayType.class)
	@Column(columnDefinition = "UUID[]")
	private UUID[]		participantIdList;
}
