package xyz.zlatanov.ravenscore.model.swordsandravens;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import xyz.zlatanov.ravenscore.domain.model.House;

public class SnrHouseLabelDeserializer extends JsonDeserializer<House> {

	@Override
	public House deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return Optional.ofNullable(p.getValueAsString())
				.map(String::trim)
				.map(v -> House.valueOf(v.toUpperCase()))
				.orElseThrow();
	}
}
