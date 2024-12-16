package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.web.service.security.TournamentId;

@Data
@Accessors(chain = true)
public class StageForm {

	private UUID	stageId;

	@NotNull
	@TournamentId
	private UUID	tournamentId;

	@NotEmpty
	@Size(max = 255)
	private String	name;

	@NotNull
	@Positive
	private Integer	qualificationCount	= 1;
}
