package xyz.zlatanov.ravenscore.model.swordsandravens;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty))
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnrViewOfGame {

	private List<SnrPlayer>	victoryTrack;

	private Integer			round;
}
