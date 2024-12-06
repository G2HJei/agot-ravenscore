package xyz.zlatanov.ravenscore.web.model;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.House;

@Data
@Accessors(fluent = true)
public class PlayerModel {

	private ParticipantModel	participant;
	private House				house;
	private Integer				rank;
	private Integer				castles;
	private Integer				landAreas;
	private Integer				supply;
	private Integer				ironThrone;
	private Integer				score;
	private Integer				penaltyPoints;
}
