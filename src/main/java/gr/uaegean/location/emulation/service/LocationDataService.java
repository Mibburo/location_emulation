package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.LocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static gr.uaegean.location.emulation.util.Constants.RTLS_API_URL;

@Slf4j
@Service
public class LocationDataService {

    public void sendLocationData(LocationDTO locationDto){
        RestTemplate restTemplate = new RestTemplate();
        if(locationDto.getIsNewPerson()){
            restTemplate.postForObject(RTLS_API_URL +"/addPersonAndDevice", locationDto, String.class);
        }
        restTemplate.postForObject(RTLS_API_URL +"/saveLocationData", locationDto, String.class);
    }

    public List<LocationDTO> getLocationData(){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<LocationDTO[]> response =
                restTemplate.getForEntity(
                        RTLS_API_URL+"/getAllPersons",
                        LocationDTO[].class);
        //List<LocationDTO> location = Arrays.asList(response.getBody());

        return Arrays.asList(response.getBody());
    }


}
