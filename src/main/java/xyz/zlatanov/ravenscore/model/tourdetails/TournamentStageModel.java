package xyz.zlatanov.ravenscore.model.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class TournamentStageModel {

    private String                 id;
    private String                 name;
    private Integer                qualificationCount;
    private String                 startDate;
    private boolean                completed            = false;
    private boolean                completable          = false;
    private String                 rankingMode;
    private List<ParticipantModel> participantModelList = new ArrayList<>();
    private List<GameModel>        gameModelList        = new ArrayList<>();
}