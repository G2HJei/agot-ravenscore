package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.web.service.security.TournamentId;

@Data
@Accessors(chain = true)
public class PlayerForm {

	private UUID		playerId;
	@NotNull
	@TournamentId
	private UUID		tournamentId;
	private UUID		tournamentStageId;
	@NotNull
	@Size(min = 2, max = 64)
	private String		name;
	private String[]	profileLinks	= new String[] {};
}
