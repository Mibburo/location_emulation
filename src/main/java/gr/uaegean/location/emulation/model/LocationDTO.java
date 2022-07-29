package gr.uaegean.location.emulation.model;

import gr.uaegean.location.emulation.model.entity.LocationTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class LocationDTO {

    private LocationTO locationTO;
    private Boolean isNewPerson;
    private String oxygenSaturation;
    private String heartBeat;
    private Boolean hasOxygenProblem;
    private Boolean hasHeartProblem;
}
