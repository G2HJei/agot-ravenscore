package xyz.zlatanov.ravenscore.service.builder.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.model.House;

@Data
@Accessors(fluent = true)
public class GameWin {

	private House	house;
	private boolean	clean;
}
