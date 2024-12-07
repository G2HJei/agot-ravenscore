package xyz.zlatanov.ravenscore.web.model.toursummary;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentSummaryModel {

	private String	id;
	private String	name;
	private Integer	numberOfParticipants;
	private String	statusLabel;
	private String	statusDate;

	public int backgroundNumber() {
		return name.length() % 4 + 1;
	}
}
