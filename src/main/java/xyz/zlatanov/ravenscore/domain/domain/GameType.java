package xyz.zlatanov.ravenscore.domain.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GameType {

	SECOND_EDITION("[2nd Ed]", "Base Game Second Edition"), //
	A_FEAST_FOR_CROWS("[AFFC]", "A Feast For Crows"),//
	A_DANCE_WITH_DRAGONS("[ADWD]", "A Dance With Dragons"), //
	MOTHER_OF_DRAGONS("[MoD]", "Mother of Dragons");

	private final String	shortname;
	private final String	longName;
}
