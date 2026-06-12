package xyz.zlatanov.ravenscore.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Data
@Accessors(fluent = true)
@Entity
public class Player {

    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(STRING)
    private House house;

    private Integer castles = 0;

    private Integer points = 0;

    private Integer penaltyPoints = 0;

    private UUID gameId;

    private UUID participantId;
}
