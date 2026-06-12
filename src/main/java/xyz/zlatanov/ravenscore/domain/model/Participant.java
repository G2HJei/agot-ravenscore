package xyz.zlatanov.ravenscore.domain.model;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Entity
public class Participant implements Cloneable {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @Type(StringArrayType.class)
    @Column(columnDefinition = "VARCHAR[]")
    private String[] profileLinks;

    private UUID replacementParticipantId;

    private BigDecimal pointsModifier = BigDecimal.ONE;

    @Override
    @SneakyThrows
    public Participant clone() {
        val clone = (Participant) super.clone();
        clone.id = null;
        clone.name = name;
        clone.profileLinks = Arrays.copyOf(profileLinks, profileLinks.length);
        clone.replacementParticipantId = null;// do not copy replacement because it happens only for one participant
        clone.pointsModifier = pointsModifier;
        return clone;
    }
}
