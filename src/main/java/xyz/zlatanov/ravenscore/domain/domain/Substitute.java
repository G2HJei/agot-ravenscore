package xyz.zlatanov.ravenscore.domain.domain;

import java.util.UUID;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
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
	private UUID		id;

	private String		name;

	@Type(StringArrayType.class)
	@Column(columnDefinition = "VARCHAR[]")
	private String[]	profileLinks;

	private UUID		tournamentId;
}
