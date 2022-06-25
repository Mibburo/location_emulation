package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.LocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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


}
