package xyz.zlatanov.ravenscore.web.model.tourdetails.admin;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.domain.GameType;

@Data
@Accessors(chain = true)
public class GameForm {

	private UUID		id;
	@NotNull
	private UUID		stageId;
	@NotNull
	private GameType	type;
	@NotEmpty
	@Size(min = 4, max = 255)
	private String		name;
	@Size(min = 4, max = 255)
	private String		link;
	private UUID[]		participantIdList	= new UUID[] {};

	public String getLink() {
		return "".equals(link) ? null : link;
	}
}
