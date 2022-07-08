package gr.uaegean.location.emulation.model;

import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class EmulationDTO {

    private Integer noOfData;
    private Double realX;
    private Double positionError;
    private Integer pathErrorPrcntg;
    private Integer speed;
    private MultipartFile imageFile;
    private double deck7Scale;
    private double deck8Scale;
    private double deck9Scale;
    private String startTimestamp;
    private List<GeofenceAttributes> colorCodeList;
    private String[][] grid;
    private Map<Integer, List<String>> endGf;
    Map<String, String> geofences;
    private Boolean hasDelay;
    private Boolean isDistance;
    private Boolean afterFirstWave;
    private Integer deck;
    private Integer oxygenProblemPrnctg;
    private Integer heartProblemPrnctg;

    public void populateDefaultGeofenceColors(){
        List<GeofenceAttributes> colorCodeList = new ArrayList<>();
        LocationDataUtils.gfMap.forEach((k,v) -> colorCodeList.add(new GeofenceAttributes(k,v)));
        setColorCodeList(colorCodeList);
    }

    public void populateDefaultEndGeofence(){
        setEndGf(LocationDataUtils.exitVal);
    }

    public void populateNewGeofenceMap(){
        Map<String, String> gfs = new HashMap<>();
        for (GeofenceAttributes gf : getColorCodeList()) {
            gfs.put(gf.getId(), gf.getName());
        }

        this.geofences = gfs;
    }
}
