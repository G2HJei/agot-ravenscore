package xyz.zlatanov.ravenscore.web.service.builder.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.House;

@Data
@Accessors(fluent = true)
public class GameWin {

	private House	house;
	private boolean	clean;
}
