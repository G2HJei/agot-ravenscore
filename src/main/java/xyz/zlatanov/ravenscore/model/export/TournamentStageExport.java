package xyz.zlatanov.ravenscore.model.export;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class TournamentStageExport {

	private String					name;
	private Integer					qualificationCount;
	private LocalDateTime			startDate;
	private Boolean					completed;
	private List<ParticipantExport>	participantExportList	= new ArrayList<>();
	private List<GameExport>		gameExportList			= new ArrayList<>();
}
