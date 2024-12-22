package xyz.zlatanov.ravenscore.web.model.export;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class TournamentExport {

	private String						name;
	private String						description;
	private Scoring						scoring;
	private Boolean						hidden;
	private String						tournamentKey;
	private LocalDate					startDate;
	private List<SubstituteExport>		substituteExportList		= new ArrayList<>();
	private List<TournamentStageExport>	tournamentStageExportList	= new ArrayList<>();
}
