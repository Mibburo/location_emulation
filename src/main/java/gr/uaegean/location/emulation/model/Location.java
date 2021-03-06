package gr.uaegean.location.emulation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @JsonProperty("xLocation")
    private String xLocation;

    @JsonProperty("yLocation")
    private String yLocation;

    private String errorLevel;
    private String isAssociated;
    private String campusId;
    private String buildingId;
    private String floorId;
    private String hashedMacAddress;
    private String geofenceId;
    private List<String> geofenceNames;
    private String timestamp;
    private String deck;

    @JsonIgnore
    public void calculateCoords(Pair<Integer,Integer> coords, double scale, double error, Integer deckNo){

        double actualX = coords.getLeft() * scale;
        double actualY = coords.getRight() * scale;

        Integer deckImageOffset = 0;
        if(deckNo != 7) deckImageOffset = 90;
        setXLocation(String.valueOf(generateCoordWithError(actualX, error) + deckImageOffset));
        setYLocation(String.valueOf(generateCoordWithError(actualY, error)));

        // grid coords for dev purposes
        /*setXLocation(String.valueOf(coords.getLeft()));
        setYLocation(String.valueOf(coords.getRight()));*/
    }

    @JsonIgnore
    public double generateCoordWithError(double coord, double error){
        double max = coord + error;
        double min = coord - error;

        return Math.random() * (max - min) + min;
    }
}