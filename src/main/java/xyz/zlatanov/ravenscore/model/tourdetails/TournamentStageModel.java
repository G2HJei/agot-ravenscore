package xyz.zlatanov.ravenscore.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentStageModel {

	private String					id;
	private String					name;
	private Integer					qualificationCount;
	private String					startDate;
	private boolean					completed				= false;
	private List<ParticipantModel>	participantModelList	= new ArrayList<>();
	private List<GameModel>			gameModelList			= new ArrayList<>();
}
