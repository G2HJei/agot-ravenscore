package xyz.zlatanov.ravenscore.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.House;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class PlayerExport {

	private House	house;
	private Integer	castles;
	private Integer	points;
	private Integer	penaltyPoints;
	private String	participantName;
}
