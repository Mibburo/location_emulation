package gr.uaegean.location.emulation.model;

import gr.uaegean.location.emulation.model.entity.LocationData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class LocationDTO {

    private LocationData locationData;
    private Boolean isNewPerson;
}
