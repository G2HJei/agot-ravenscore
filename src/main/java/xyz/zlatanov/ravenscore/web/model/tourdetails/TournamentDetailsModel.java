package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;

@Data
@Accessors(fluent = true)
public class TournamentDetailsModel {

	private String						id;
	private String						name;
	private String						description;
	private Scoring						scoring;
	private String						startDate;
	private Boolean						adminUnlocked				= false;
	private List<SubstituteModel>		substituteModelList			= new ArrayList<>();
	private List<TournamentStageModel>	tournamentStageModelList	= new ArrayList<>();
}
