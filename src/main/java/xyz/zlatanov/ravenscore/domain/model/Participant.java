package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.CascadeType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Participant {

	@Id
	@UuidGenerator
	private UUID					id;

	@Column(length = 64, nullable = false)
	private String					name;

	@Column(nullable = false)
	private String					profileLink;

	@Column(nullable = false)
	private boolean					substitute;

	@OneToOne
	@JoinColumn(name = "replacement_participant_id")
	@ToString.Exclude
	private Participant				replacementParticipant;

	@OneToMany(cascade = ALL)
	@JoinColumn(name = "participant_id")
	@ToString.Exclude
	private List<Player>			playerList			= new ArrayList<>();

	@ManyToMany(cascade = { PERSIST, MERGE })
	@JoinTable(name = "participant_tournament_stage", joinColumns = @JoinColumn(name = "participant_id"), inverseJoinColumns = @JoinColumn(name = "tournament_stage_id"))
	@ToString.Exclude
	private List<TournamentStage>	tournamentStageList	= new ArrayList<>();

	@ManyToMany(cascade = { PERSIST, MERGE })
	@JoinTable(name = "participant_game", joinColumns = @JoinColumn(name = "participant_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
	@ToString.Exclude
	private List<Game>				gameList			= new ArrayList<>();
}
