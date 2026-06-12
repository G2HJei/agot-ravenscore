package xyz.zlatanov.ravenscore.model.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.domain.model.GameType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class GameModel {

    private String            id;
    private String            name;
    private GameType          type;
    private String            link;
    private Integer           round;
    private Boolean           completed;
    private Boolean           playersRevealed;
    private List<String>      participantIdList;
    private BigDecimal        pointsModifier;
    private List<PlayerModel> playerModelList = new ArrayList<>();

}
