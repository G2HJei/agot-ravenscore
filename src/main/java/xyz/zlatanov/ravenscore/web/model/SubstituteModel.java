package xyz.zlatanov.ravenscore.web.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SubstituteModel {

	private String	id;
	private String	name;
	private String	profileLink;
}
