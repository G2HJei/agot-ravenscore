package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SubstituteModel {

	private String			name;
	private List<String>	profileLinks;
}
