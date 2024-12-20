package xyz.zlatanov.ravenscore.web.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class ParticipantExport {

	private String		name;
	private String[]	profileLinks;
	private String		replacementParticipantName;

}
