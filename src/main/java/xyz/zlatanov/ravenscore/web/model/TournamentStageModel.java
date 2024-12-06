package xyz.zlatanov.ravenscore.web.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentStageModel {

	private String					id;
	private String					name;
	private List<ParticipantModel>	participantModelList	= new ArrayList<>();
}
