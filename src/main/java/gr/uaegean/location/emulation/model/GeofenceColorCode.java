package gr.uaegean.location.emulation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class GeofenceColorCode {

    private String id;
    private String name;

    public GeofenceColorCode(String id, String name){
        this.id=id;
        this.name=name;
    }
}
