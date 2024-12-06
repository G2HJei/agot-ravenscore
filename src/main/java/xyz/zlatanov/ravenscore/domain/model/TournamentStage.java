package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.CascadeType.ALL;

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
public class TournamentStage {

	@Id
	@UuidGenerator
	private UUID				id;

	@Column(nullable = false)
	private String				name;

	@OneToMany(cascade = ALL)
	@JoinColumn(name = "tournament_stage_id", nullable = false)
	private List<Game>			gameList		= new ArrayList<>();

	@ManyToMany(mappedBy = "tournamentStageList")
	private List<Participant>	participantList	= new ArrayList<>();

	public void addParticipant(Participant participant) {
		participantList.add(participant);
		participant.tournamentStageList().add(this);
	}

	public void removeParticipant(Participant participant) {
		participantList.remove(participant);
		participant.tournamentStageList().remove(this);
	}
}
