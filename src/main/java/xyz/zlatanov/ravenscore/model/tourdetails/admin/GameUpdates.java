package xyz.zlatanov.ravenscore.model.tourdetails.admin;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class GameUpdates {

	private UUID				gameId;
	private String				gameName;
	private Integer				round;
	private Boolean				completed;
	private List<PlayerRanking>	playerRankingList;
}
