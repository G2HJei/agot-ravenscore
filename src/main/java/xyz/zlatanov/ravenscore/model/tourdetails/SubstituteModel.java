package xyz.zlatanov.ravenscore.model.tourdetails;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class SubstituteModel {

    private String            id;
    private String            name;
    private BigDecimal        pointsModifier;
    private List<ProfileLink> profileLinks = new ArrayList<>();
}
