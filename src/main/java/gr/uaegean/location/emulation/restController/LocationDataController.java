package gr.uaegean.location.emulation.restController;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.model.GeofenceColorCode;
import gr.uaegean.location.emulation.model.LocationServiceDTO;
import gr.uaegean.location.emulation.service.MappingService;
import gr.uaegean.location.emulation.service.PathingService;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class LocationDataController {

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LocationDataUtils locationDataUtils;

    @Autowired
    private PathingService pathingService;

    @PostMapping("/getGeofence")
    public GeofenceColorCode getGeofence(@RequestBody LocationServiceDTO locationServiceDTO) throws IOException {
        String[][] grid = mappingService.convertImageToColorArray();

        Double gridX = Double.parseDouble(locationServiceDTO.getXCoord()) / locationDataUtils.calculateScale(66, grid.length);
        Double gridY = Double.parseDouble(locationServiceDTO.getYCoord()) / locationDataUtils.calculateScale(66, grid.length);

        GeofenceColorCode geofence = new GeofenceColorCode(grid[gridX.intValue()][gridY.intValue()], LocationDataUtils.gfMap.get(grid[gridX.intValue()][gridY.intValue()]));

        return geofence;
    }

    @PostMapping("/getDistance")
    public String getDistance(@RequestBody LocationServiceDTO locationServiceDTO) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String[][] grid = mappingService.convertImageToColorArray();

        Double gridX = Double.parseDouble(locationServiceDTO.getXCoord()) / locationDataUtils.calculateScale(66, grid.length);
        Double gridY = Double.parseDouble(locationServiceDTO.getYCoord()) / locationDataUtils.calculateScale(66, grid.length);

        Pair<Integer, Integer> startLocation = new ImmutablePair<>(gridX.intValue(), gridY.intValue());
        EmulationDTO dto = new EmulationDTO();
        dto.setEndGf(locationServiceDTO.getMusterStationId());
        dto.setIsDistance(true);

        return String.valueOf(pathingService.minDistance(grid, startLocation.getLeft(), startLocation.getRight(), null, null, dto)
                * locationDataUtils.calculateScale(66, grid.length));
    }

    @PostMapping("/getPassengerSpeed")
    public String getPassengerSpeed(@RequestBody List<LocationServiceDTO> dto) {

        List<Double> speeds = new ArrayList<>();
        for(int i = dto.size()-1; i >=0; i--){
            if(i-1 < 0){
                break;
            }
            Double yDist = Math.abs(Double.parseDouble(dto.get(i).getYCoord())
                    - Double.parseDouble(dto.get(i-1).getYCoord()));
            Double xDist = Math.abs(Double.parseDouble(dto.get(i).getXCoord())
                    - Double.parseDouble(dto.get(i-1).getXCoord()));

            Double coordDist = Math.hypot(yDist, xDist);

            LocalDateTime start = LocationDataUtils.stringToLDT(dto.get(i-1).getTimestamp());
            LocalDateTime end = LocationDataUtils.stringToLDT(dto.get(i).getTimestamp());
            Long duration = Duration.between(start, end).getSeconds();
            Double speed = coordDist / Double.valueOf(duration);
            speeds.add(speed);
        }

        if(!speeds.isEmpty()){
            return String.valueOf(speeds.stream().mapToDouble(Double::doubleValue).sum() / speeds.size());
        }

        return null;
    }

    @PostMapping("/runEmulation")
    public void runEmulation(@RequestBody EmulationDTO dto) throws IOException {

        String[][] grid = mappingService.convertImageToColorArray();
        dto.setScale(locationDataUtils.calculateScale(dto.getRealX() == null? 66: dto.getRealX(), grid.length));

        dto.setSpeed(locationDataUtils.getRandomSpeed());
        dto.setPathErrorPrcntg(dto.getPathErrorPrcntg() == null? 3 : dto.getPathErrorPrcntg());
        dto.setPositionError(dto.getPositionError() == null? 0.5 : dto.getPositionError());

        //format timestamp
        dto.setStartTimestamp(dto.getStartTimestamp() == null? LocationDataUtils.dateToString(LocalDateTime.now()) :
                dto.getStartTimestamp().replace("T", " "));
        log.info("start timestamp :{}", dto.getStartTimestamp());

        if(dto.getColorCodeList() != null && !dto.getColorCodeList().isEmpty()) {
            dto.populateNewGeofenceMap();
        }
        dto.setGeofences(dto.getGeofences() == null? LocationDataUtils.gfMap : dto.getGeofences());
        dto.setIsDistance(false);
        dto.setHasDelay(dto.getHasDelay() == null? false : dto.getHasDelay());
        //generate faulty end location
        for(int i=0; i<dto.getNoOfData(); i++){

            Pair<Integer, Integer> startLocation = locationDataUtils.generateRandomStartPoint(grid);

            new Thread(() -> {
                try {
                    if (dto.getPathErrorPrcntg() >= (int) (Math.random() * (100))){
                        Pair<Integer, Integer> endLocation = locationDataUtils.generateFaultyEndPoint(grid);
                        log.info("start Location :{}, end location :{}", startLocation, endLocation);
                        pathingService.minDistance(grid, startLocation.getLeft(), startLocation.getRight(), endLocation.getLeft(), endLocation.getRight(), dto);
                    } else {
                        pathingService.minDistance(grid, startLocation.getLeft(), startLocation.getRight(), null, null, dto);
                    }
                } catch (NoSuchAlgorithmException e) {
                    log.error(e.getMessage());
                } catch (InvalidKeyException e) {
                    log.error(e.getMessage());
                }
            }).start();

        }
    }
}
