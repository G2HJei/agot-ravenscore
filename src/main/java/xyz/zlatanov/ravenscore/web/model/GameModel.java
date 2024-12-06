package xyz.zlatanov.ravenscore.web.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.GameType;

@Data
@Accessors(fluent = true)
public class GameModel {

	private String				id;
	private String				name;
	private GameType			gameType;
	private String				gameLink;
	private Integer				round;
	private List<PlayerModel>	playerModelList	= new ArrayList<>();
}
