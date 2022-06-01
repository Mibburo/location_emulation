package gr.uaegean.location.emulation.restController;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.service.MappingService;
import gr.uaegean.location.emulation.service.PathingService;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
@Controller
public class MappingController{

    @Autowired
    private MappingService mappingService;

    @Autowired
    private PathingService pathingService;

    @Autowired
    private LocationDataUtils locationDataUtils;

    @GetMapping("/map")
    public ModelAndView mapImage(@ModelAttribute EmulationDTO dto) throws IOException {

        dto.setGrid(mappingService.convertDeck7ToColorArray());

        return new ModelAndView("mapVisualization", "locationData", dto);
    }

    /*@GetMapping("/emulation")
    public ModelAndView prepareEmulation() throws IOException {

        EmulationDTO dto = new EmulationDTO();
        dto.populateDefaultGeofenceColors();
        dto.populateDefaultEndGeofence();
        return new ModelAndView("emulation", "locationData", dto);
    }*/

    /*@PostMapping("/generateData")
    public ModelAndView generateData(@ModelAttribute EmulationDTO dto, HttpServletRequest req, ModelMap model) throws IOException {

        *//*int[][] grid = {  { 0, 255, 0, 255 , 255},
                            { 255, 0, 255, 255, 255 },
                            { 0, 255, 255, 255, 255 },
                            { 255 , 255, 255, 255, 255 },
                            { 255 , 255, 255, 255, 255 }};*//*

        //String[][] grid = mappingService.convertImageToColorArray(dto);
        String[][] grid = mappingService.convertImageToColorArray();

        dto.setScale(locationDataUtils.calculateScale(dto.getRealX(), grid.length));

        dto.setSpeed(locationDataUtils.getRandomSpeed());

        //format timestamp
        dto.setStartTimestamp(dto.getStartTimestamp() == null? LocationDataUtils.dateToString(LocalDateTime.now()) :
                dto.getStartTimestamp().replace("T", " "));
        log.info("start timestamp :{}", dto.getStartTimestamp());

        if(dto.getColorCodeList() != null && !dto.getColorCodeList().isEmpty()) {
            dto.populateNewGeofenceMap();
        }

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
        return new ModelAndView("emulation", "locationData", dto);
    }*/

}
