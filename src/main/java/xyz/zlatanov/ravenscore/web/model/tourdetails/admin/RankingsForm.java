package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RankingsForm {

	private UUID				gameId;
	private Boolean				completed;
	private List<PlayerScoring>	playerScoringList;
}
