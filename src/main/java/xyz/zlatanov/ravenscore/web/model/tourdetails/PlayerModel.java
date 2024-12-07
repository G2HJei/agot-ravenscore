package xyz.zlatanov.ravenscore.web.model.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PlayerModel {

	private String	name;
	private String	house;
	private Integer	castles;
	private Integer	score;
	private Integer	penaltyPoints;
}
