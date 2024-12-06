package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Tournament {

	@Id
	@UuidGenerator
	private UUID					id;

	@Column(nullable = false)
	private String					name;

	@Column(length = 2000)
	private String					description;

	@Enumerated(STRING)
	@Column(length = 16, nullable = false)
	private Ruleset					ruleset;

	@Column(length = 32, nullable = false)
	private String					tournamentKey;

	@OneToMany(cascade = ALL)
	@JoinColumn(name = "tournament_id", nullable = false)
	private List<Participant>		participantList		= new ArrayList<>();

	@OneToMany(cascade = ALL)
	@JoinColumn(name = "tournament_id", nullable = false)
	private List<TournamentStage>	tournamentStageList	= new ArrayList<>();
}
