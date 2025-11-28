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
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Game {

	@Id
	@UuidGenerator
	private UUID		id;

	private String		name;

	@Enumerated(STRING)
	private GameType	type;

	private String		link;

	private Integer		round;

	private UUID		tournamentStageId;

	private Boolean		completed;

	@Type(UUIDArrayType.class)
	@Column(columnDefinition = "UUID[]")
	private UUID[]		participantIdList;
}
