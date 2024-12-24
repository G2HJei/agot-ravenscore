package xyz.zlatanov.ravenscore.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;
import xyz.zlatanov.ravenscore.model.statistics.TournamentStatistics;

@Data
@Accessors(fluent = true)
public class TournamentDetailsModel {

	private String						id;
	private String						name;
	private String						description;
	private Boolean						hidden;
	private Scoring						scoring;
	private String						startDate;
	private Boolean						adminUnlocked				= false;
	private String						tournamentKey;
	private String						winnerParticipantId;
	private String						rankingMode;
	private List<SubstituteModel>		substituteModelList			= new ArrayList<>();
	private List<TournamentStageModel>	tournamentStageModelList	= new ArrayList<>();
	private TournamentStatistics		tournamentStatistics;
}
