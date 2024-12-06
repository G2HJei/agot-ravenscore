package xyz.zlatanov.ravenscore.domain.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Entity
public class Game {

	@Id
	@UuidGenerator
	private UUID				id;

	@Column(nullable = false)
	private String				name;

	@Enumerated(STRING)
	@Column(length = 32, nullable = false)
	private GameType			gameType;

	@Column(nullable = false)
	private String				gameLink;

	private short				currentRound;

	@NotEmpty
	@OneToMany(cascade = ALL)
	@JoinColumn(name = "game_id", nullable = false)
	private List<Player>		playerList		= new ArrayList<>();

	@NotEmpty
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
