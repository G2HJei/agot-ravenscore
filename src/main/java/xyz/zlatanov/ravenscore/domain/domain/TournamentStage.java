package xyz.zlatanov.ravenscore.domain.domain;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class TournamentStage {

	@Id
	@UuidGenerator
	private UUID		id;

	private String		name;

	private Integer		stageNumber;

	private LocalDate	startDate	= LocalDate.now();

	private UUID		tournamentId;

	@NotEmpty
	@Type(UUIDArrayType.class)
	@Column(columnDefinition = "UUID[]")
	private UUID[]		participantIdList;
}
