package xyz.zlatanov.ravenscore.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Entity
public class PlayerInactivity {

    // start of the dummy entries for penalties given before inactivity tracking existed
    public static final LocalDateTime UNTRACKED_START = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    @Id
    @UuidGenerator
    private UUID id;

    private UUID playerId;

    private LocalDateTime startTime;

    private Integer durationHours;
}
