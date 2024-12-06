package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.EnumType.STRING;

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
	private String	id;

	@Enumerated(STRING)
	@Column(length = 16, nullable = false)
	private House	house;

	private Short	rank;

	private Short	castles;

	private Short	landAreas;

	private Short	supply;

	private Short	ironThrone;

	private Short	score;

	@Column(nullable = false)
	private short	penaltyPoints	= 0;
}
