package xyz.zlatanov.ravenscore.model.swordsandravens;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

// SnR timestamps are in UTC (e.g. 2026-10-21T11:06:10.327402Z), convert them to the server's local time
public class SnrLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return LocalDateTime.ofInstant(Instant.parse(p.getValueAsString()), ZoneId.systemDefault());
	}
}
