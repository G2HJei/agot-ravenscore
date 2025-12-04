package xyz.zlatanov.ravenscore.model.swordsandravens;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.model.House;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty))
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnrPlayer {

	@JsonDeserialize(using = SnrHouseLabelDeserializer.class)
	private House	house;

	@JsonProperty("player")
	private String	name;

	private Integer	victoryPoints;

	private Integer	totalLandAreas;

	private Integer	supplyLevel;

	private Integer	ironThronePosition;
}
