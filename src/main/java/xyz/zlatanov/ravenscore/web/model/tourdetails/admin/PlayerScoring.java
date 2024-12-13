package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerScoring {

	private UUID	playerId;
	private UUID	participantId;
	private Integer	castles;
	private Integer	penaltyPoints;
	private Integer	score;
}
