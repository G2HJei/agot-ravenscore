package xyz.zlatanov.ravenscore.model.toursummary;

import static java.lang.Math.abs;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentSummaryModel {

	private String	id;
	private String	name;
	private Long	numberOfParticipants;
	private String	statusLabel;
	private String	statusDate;
	private boolean	pinned;

	public int backgroundNumber() {
		return abs(name.hashCode()) % 4 + 1;
	}
}
