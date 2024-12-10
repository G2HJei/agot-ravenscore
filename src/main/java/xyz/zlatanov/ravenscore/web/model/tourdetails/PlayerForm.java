package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerForm {

	private UUID		playerId;
	@NotEmpty
	private UUID		tournamentId;
	private UUID		tournamentStageId;
	@NotEmpty
	@Size(min = 2, max = 64)
	private String		name;
	private String[]	profileLinks	= new String[] {};
}
