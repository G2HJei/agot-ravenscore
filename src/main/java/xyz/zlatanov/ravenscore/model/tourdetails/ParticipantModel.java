package xyz.zlatanov.ravenscore.model.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.service.builder.tourdetails.GameWin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class ParticipantModel {

    private String            id;
    private String            name;
    private BigDecimal        pointsModifier;
    private String            replacedLabel;
    private List<ProfileLink> profileLinks = new ArrayList<>();
    private Integer           games;
    private Integer           points;
    private BigDecimal        pointsDecimal;
    private Integer           penaltyPoints;
    private List<GameWin>     wins;
    private Integer           cleanWins;
    private Double            avgPtsDouble;
    private String            avgPoints;

    public Integer score() {
        return points - penaltyPoints;
    }

    public BigDecimal scoreDecimal() {
        return pointsDecimal.subtract(BigDecimal.valueOf(penaltyPoints));
    }

}
