package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentStageModel {

	private String					name;
	private Integer					qualificationCount;
	private String					startDate;
	private List<ParticipantModel>	participantModelList	= new ArrayList<>();
	private List<GameModel>			gameModelList			= new ArrayList<>();
}
