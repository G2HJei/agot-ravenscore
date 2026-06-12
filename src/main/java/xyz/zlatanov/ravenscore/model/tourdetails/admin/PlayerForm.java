package xyz.zlatanov.ravenscore.model.tourdetails.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.security.TournamentId;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PlayerForm {

    private UUID       playerId;
    @NotNull
    @TournamentId
    private UUID       tournamentId;
    private UUID       tournamentStageId;
    @NotNull
    @Size(min = 2, max = 64)
    private String     name;
    @NotNull
    @Min(0)
    @Max(1)
    private BigDecimal pointsModifier;
    private String[]   profileLinks = new String[]{};
}
