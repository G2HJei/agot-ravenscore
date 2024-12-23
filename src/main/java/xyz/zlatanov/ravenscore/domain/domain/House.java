package xyz.zlatanov.ravenscore.domain.domain;

import static xyz.zlatanov.ravenscore.Utils.capitalizeFirstLetter;

public enum House {

	STARK, GREYJOY, LANNISTER, TYRELL, MARTELL, BARATHEON, ARRYN, TARGARYEN, BOLTON;

	public String label() {
		return capitalizeFirstLetter(this.name());
	}
}
