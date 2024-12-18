package xyz.zlatanov.ravenscore.web.service.builder;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GameWin {

	private boolean clean;
}
