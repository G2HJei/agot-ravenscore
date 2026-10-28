package xyz.zlatanov.ravenscore.model.export;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class PlayerInactivityExport {

	private LocalDateTime	startTime;
	private Integer			durationHours;
}
