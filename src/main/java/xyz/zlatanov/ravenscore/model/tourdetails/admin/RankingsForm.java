package xyz.zlatanov.ravenscore.model.tourdetails.admin;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RankingsForm {

	@NotNull
	private UUID						gameId;
	@NotNull
	private Boolean						completed;
	@NotEmpty
	private List<@Valid PlayerRanking>	playerRankingList;

}
