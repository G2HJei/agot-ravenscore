package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerRanking {

	@NotNull
	private UUID	playerId;
	private UUID	participantId;
	private Integer	points			= 0;
	private Integer	castles			= 0;
	private Integer	penaltyPoints	= 0;
}
