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
public class GeofenceAttributes {

    private String id;
    private String name;
    private Integer capacity;
    private Integer xStart;
    private Integer xEnd;
    private Integer yStart;
    private Integer yEnd;
    private Integer space;

    public GeofenceAttributes(String id, String name){
        this.id=id;
        this.name=name;
    }
}
