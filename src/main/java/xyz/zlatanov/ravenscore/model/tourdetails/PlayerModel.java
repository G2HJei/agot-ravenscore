package xyz.zlatanov.ravenscore.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PlayerModel {

	private String				id;
	private String				name;
	private String				replacedLabel;
	private String				house;
	private Integer				castles;
	private Integer				score;
	private Integer				penaltyPoints;
	private String				participantId;
	private List<ProfileLink>	profileLinks	= new ArrayList<>();
}
