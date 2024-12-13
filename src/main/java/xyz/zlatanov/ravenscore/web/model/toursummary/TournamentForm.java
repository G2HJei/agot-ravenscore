package xyz.zlatanov.ravenscore.web.model.toursummary;

import static xyz.zlatanov.ravenscore.domain.domain.Scoring.RANKING_PLUS_CASTLES;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.Scoring;

@Data
@Accessors(chain = true)
public class TournamentForm {

	UUID			id;
	@NotEmpty
	@Size(min = 8, max = 100)
	private String	name;
	@NotNull
	private Scoring	scoring	= RANKING_PLUS_CASTLES;
	@Size(max = 2000)
	private String	description;
	@NotNull
	private Boolean	hidden;
	@NotEmpty
	@Size(min = 8, max = 32)
	private String	tournamentKey;
}
