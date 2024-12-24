package xyz.zlatanov.ravenscore.model.statistics;

import java.math.BigDecimal;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class HouseData {

	private String		label;
	private BigDecimal	winPercent;
	private BigDecimal	averagePoints;
}
