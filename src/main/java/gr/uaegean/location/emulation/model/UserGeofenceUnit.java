package gr.uaegean.location.emulation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class UserGeofenceUnit implements Serializable { //like location

    @Field(type = Text)
    private String gfEvent;
    @Field(type = Text)
    private String gfId;
    @Field(type = Text)
    private String gfName;
    @Field(type = Text)
    private String macAddress;
    @Field(type = Text)
    private String isAssociated;
    @Field(type = Text)
    private String dwellTime;
    //    private List<AccessPoint> apInfo;
    @Field(type = Text)
    private String hashedMacAddress;
    @Field(type = Text)
    private String timestamp;
    @Field(type = Text)
    private String deck;

}