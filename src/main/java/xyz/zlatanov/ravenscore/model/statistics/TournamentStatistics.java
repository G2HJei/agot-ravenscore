package xyz.zlatanov.ravenscore.model.statistics;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class TournamentStatistics {

	private List<String>	labels		= new ArrayList<>();
	private List<Dataset>	datasets	= new ArrayList<>();
}
