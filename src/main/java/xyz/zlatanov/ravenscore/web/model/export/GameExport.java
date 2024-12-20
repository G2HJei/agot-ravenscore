package xyz.zlatanov.ravenscore.web.model.export;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.GameType;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class GameExport {

	private String				name;
	private GameType			type;
	private String				link;
	private Integer				round;
	private Boolean				completed;
	private List<String>		participantNameList;
	private List<PlayerExport>	playerExportList;

}
