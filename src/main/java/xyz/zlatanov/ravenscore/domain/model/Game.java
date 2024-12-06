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
public class Game {

	@Id
	@UuidGenerator
	private UUID				id;

	@OneToMany(cascade = ALL)
	@JoinColumn(name = "game_id", nullable = false)
	private List<Player>		playerList		= new ArrayList<>();

	@ManyToMany(mappedBy = "gameList")
	private List<Participant>	participantList	= new ArrayList<>();

	public void addParticipant(Participant participant) {
		participantList.add(participant);
		participant.gameList().add(this);
	}

	public void removeParticipant(Participant participant) {
		participantList.remove(participant);
		participant.gameList().remove(this);
	}
}
