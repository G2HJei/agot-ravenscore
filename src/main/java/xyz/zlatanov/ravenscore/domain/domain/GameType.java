package xyz.zlatanov.ravenscore.domain.domain;

import static xyz.zlatanov.ravenscore.domain.domain.House.*;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GameType {

	SECOND_EDITION("[2nd Ed]", "Base Game 2nd Edition"), //
	A_FEAST_FOR_CROWS("[AFFC]", "A Feast For Crows"),//
	A_DANCE_WITH_DRAGONS("[ADWD]", "A Dance With Dragons"), //
	MOTHER_OF_DRAGONS("[MoD]", "Mother of Dragons");

	private final String	shortname;
	private final String	longName;

	public List<House> houses() {
		return switch (this) {
			case SECOND_EDITION, A_DANCE_WITH_DRAGONS -> List.of(STARK, GREYJOY, LANNISTER, TYRELL, MARTELL, BARATHEON);
			case A_FEAST_FOR_CROWS -> List.of(STARK, LANNISTER, BARATHEON, ARRYN);
			case MOTHER_OF_DRAGONS -> List.of(STARK, GREYJOY, LANNISTER, TYRELL, MARTELL, BARATHEON, ARRYN, TARGARYEN);
		};
	}
}
