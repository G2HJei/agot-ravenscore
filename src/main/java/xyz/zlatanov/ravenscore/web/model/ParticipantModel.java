package xyz.zlatanov.ravenscore.web.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ParticipantModel {

	private String				id;
	private String				name;
	private String				profileLink;
	private ParticipantModel	replacement;
}
