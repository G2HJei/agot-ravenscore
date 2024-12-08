package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.GameType;

@Data
@Accessors(fluent = true)
public class GameModel {

	private String				name;
	private GameType			type;
	private String				link;
	private String				round;
	private Boolean				completed;
	private Boolean				playersRevealed;
	private List<String>		participantIdList;
	private List<PlayerModel>	playerModelList	= new ArrayList<>();

}
