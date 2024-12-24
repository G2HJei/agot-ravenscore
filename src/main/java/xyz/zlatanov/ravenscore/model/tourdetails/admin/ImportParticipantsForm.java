package xyz.zlatanov.ravenscore.model.tourdetails.admin;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImportParticipantsForm {

	@NotNull
	private UUID	stageId;
	private UUID[]	participantIdList	= new UUID[] {};
}
