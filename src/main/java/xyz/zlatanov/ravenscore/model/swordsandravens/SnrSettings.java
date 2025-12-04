package xyz.zlatanov.ravenscore.model.swordsandravens;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty))
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnrSettings {

    private String setupId;

    private Integer playerCount;

    private Boolean pbem;

    private Boolean randomHouses;

    private Boolean draftHouseCards;

    private Boolean randomDraft;

    private Boolean perpetuumRandom;

    private Integer selectedDraftDecks;

    private Boolean fogOfWar;

    private Boolean dragonWar;

    private Boolean dragonRevenge;
}
