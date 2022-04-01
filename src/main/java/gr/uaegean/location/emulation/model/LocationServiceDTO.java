package gr.uaegean.location.emulation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class LocationServiceDTO {

    @JsonProperty("xCoord")
    private String xCoord;
    @JsonProperty("yCoord")
    private String yCoord;
    private String deck;
    private String timestamp;
    private String musterStationId;
}
