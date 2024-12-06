package xyz.zlatanov.ravenscore.domain.model;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Player {

	@Id
	@UuidGenerator
	private String id;

}
