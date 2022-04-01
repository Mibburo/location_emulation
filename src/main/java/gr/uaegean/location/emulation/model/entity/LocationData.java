package gr.uaegean.location.emulation.model.entity;

import gr.uaegean.location.emulation.model.Geofence;
import gr.uaegean.location.emulation.model.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class LocationData {

    /*@Id
    private String id;*/

    private String macAddress;
    private String hashedMacAddress;
    private Geofence geofence;
    private Location location;

}