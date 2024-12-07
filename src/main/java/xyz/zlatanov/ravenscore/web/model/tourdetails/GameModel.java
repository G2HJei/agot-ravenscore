package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GameModel {

	private String				name;
	private String				type;
	private String				link;
	private String				round;
	private List<PlayerModel>	playerModelList	= new ArrayList<>();
}
