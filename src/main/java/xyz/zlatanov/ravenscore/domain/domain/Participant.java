package xyz.zlatanov.ravenscore.domain.domain;

import java.util.Arrays;
import java.util.UUID;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Participant implements Cloneable {

	@Id
	@UuidGenerator
	private UUID		id;

	private String		name;

	@Type(StringArrayType.class)
	@Column(columnDefinition = "VARCHAR[]")
	private String[]	profileLinks;

	private UUID		replacementParticipantId;

	@Override
	@SneakyThrows
	public Participant clone() {
		val clone = (Participant) super.clone();
		clone.id = null;
		clone.name = name;
		clone.profileLinks = Arrays.copyOf(profileLinks, profileLinks.length);
		clone.replacementParticipantId = null;// do not copy replacement because it happens only for one participant
		return clone;
	}
}
