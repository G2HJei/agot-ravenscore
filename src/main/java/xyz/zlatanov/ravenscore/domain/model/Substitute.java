package xyz.zlatanov.ravenscore.domain.model;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Entity
public class Substitute {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    private BigDecimal pointsModifier = BigDecimal.ONE;

    @Type(StringArrayType.class)
    @Column(columnDefinition = "VARCHAR[]")
    private String[] profileLinks;

    private UUID tournamentId;
}
