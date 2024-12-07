package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ParticipantModel {

	private String			name;
	private List<String>	profileLinks	= new ArrayList<>();
	private Integer			games;
	private Integer			points;
	private Integer			penaltyPoints;
	private Integer			wins;
	private String			avgPoints;
}
