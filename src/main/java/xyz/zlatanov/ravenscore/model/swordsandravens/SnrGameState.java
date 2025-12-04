package xyz.zlatanov.ravenscore.model.swordsandravens;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty))
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnrGameState {

    private UUID id;

    private String name;

    @JsonProperty("view_of_game")
    private SnrViewOfGame viewOfGame;

    private SnrState state;
}
