package xyz.zlatanov.ravenscore.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.model.GameType;

@Data
@Accessors(fluent = true)
public class GameModel {

	private String				id;
	private String				name;
	private GameType			type;
	private String				link;
	private Integer				round;
	private Boolean				completed;
	private Boolean				playersRevealed;
	private List<String>		participantIdList;
	private List<PlayerModel>	playerModelList	= new ArrayList<>();

}
