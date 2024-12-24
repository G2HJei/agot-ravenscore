package xyz.zlatanov.ravenscore.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipantExport {

	@EqualsAndHashCode.Include
	private String		name;
	private String[]	profileLinks;
	private String		replacementParticipantName;

}
