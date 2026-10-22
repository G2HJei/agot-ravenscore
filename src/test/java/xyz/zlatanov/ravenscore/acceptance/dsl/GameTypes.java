package xyz.zlatanov.ravenscore.acceptance.dsl;

import lombok.val;
import xyz.zlatanov.ravenscore.domain.model.House;

import java.util.Arrays;
import java.util.List;

/**
 * Translates the game type as players call it into the option shown in the game form.
 */
final class GameTypes {

    static final List<String> HOUSE_NAMES = Arrays.stream(House.values()).map(Enum::name).map(String::toLowerCase).toList();

    private GameTypes() {
    }

    static String optionLabel(String type) {
        return switch (Params.normalize(type)) {
            case "2nd edition", "second edition", "base game", "base game 2nd edition" -> "Base Game 2nd Edition";
            case "a feast for crows", "feast for crows", "affc" -> "A Feast For Crows";
            case "a dance with dragons", "dance with dragons", "adwd" -> "A Dance With Dragons";
            case "mother of dragons", "mod" -> "Mother of Dragons";
            default -> throw new IllegalArgumentException("Unknown game type \"" + type
                    + "\"; expected 2nd edition, a feast for crows, a dance with dragons or mother of dragons");
        };
    }

    /**
     * House names as Ravenscore displays them, e.g. "stark" or "STARK" becomes "Stark".
     */
    static String house(String house) {
        val lower = house.trim().toLowerCase();
        assert HOUSE_NAMES.contains(lower) : "Must use a known house";
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
