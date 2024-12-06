package xyz.zlatanov.ravenscore.web.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;

@Data
@Accessors(fluent = true)
public class TournamentModel {

	private String						id;
	private String						name;
	private String						description;
	private Scoring						scoring;
	private boolean						hidden;
	private List<SubstituteModel>		substituteModelList			= new ArrayList<>();
	private List<TournamentStageModel>	tournamentStageModelList	= new ArrayList<>();
}
