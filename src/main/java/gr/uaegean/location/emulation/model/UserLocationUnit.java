package gr.uaegean.location.emulation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class UserLocationUnit implements Serializable {

    @Field(type = Text)
    @JsonProperty("xLocation")
    private String xLocation;
    @Field(type = Text)
    @JsonProperty("yLocation")
    private String yLocation;
    @Field(type = Text)
    private String errorLevel;
    @Field(type = Text)
    private String isAssociated;
    @Field(type = Text)
    private String campusId;
    @Field(type = Text)
    private String buildingId;
    @Field(type = Text)
    private String floorId;
    @Field(type = Text)
    private String hashedMacAddress;
    @Field(type = Text)
    private String geofenceId;
    //    @Field( type = FieldType.Nested)
    private List<String> geofenceNames;
    // private String rssiVal;
    @Field(type = Text)
    private String timestamp;

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
