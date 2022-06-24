package gr.uaegean.location.emulation.model.entity;

import gr.uaegean.location.emulation.model.UserGeofenceUnit;
import gr.uaegean.location.emulation.model.UserLocationUnit;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationTO  implements Serializable {

    private String macAddress;
    private String hashedMacAddress;
    private UserGeofenceUnit geofence;
    private UserLocationUnit location;
}
