package gr.uaegean.location.emulation.restController;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.model.GeofenceAttributes;
import gr.uaegean.location.emulation.model.LocationServiceDTO;
import gr.uaegean.location.emulation.service.LocationGenerationService;
import gr.uaegean.location.emulation.service.MappingService;
import gr.uaegean.location.emulation.service.PathingService;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
public class LocationDataController {

    private MappingService mappingService;
    private LocationDataUtils locationDataUtils;
    private PathingService pathingService;
    private LocationGenerationService locationGenerationService;
    static Random random = new Random();

    @Autowired
    LocationDataController(MappingService mappingService,
                           LocationDataUtils locationDataUtils,
                           PathingService pathingService,
                           LocationGenerationService locationGenerationService){
        this.mappingService = mappingService;
        this.locationDataUtils = locationDataUtils;
        this.pathingService = pathingService;
        this.locationGenerationService = locationGenerationService;

    }

    @PostMapping("/getGeofence")
    public GeofenceAttributes getGeofence(@RequestBody LocationServiceDTO locationServiceDTO) throws IOException {
        EmulationDTO dto = getGridAndWidth(locationServiceDTO);
        double scale = locationDataUtils.calculateScale(dto.getRealX(), dto.getGrid().length);
        Double xCoord = getCorrectXCoord(locationServiceDTO.getXCoord(), Integer.valueOf(locationServiceDTO.getDeck()));
        Double yCoord = getCorrectYCoord(locationServiceDTO.getYCoord(), Integer.valueOf(locationServiceDTO.getDeck()));

        Double gridX = xCoord / scale;
        Double gridY = yCoord / scale;
        GeofenceAttributes geofence = new GeofenceAttributes(dto.getGrid()[gridX.intValue()][gridY.intValue()],
                LocationDataUtils.gfMap.get(dto.getGrid()[gridX.intValue()][gridY.intValue()]));

        return geofence;
    }

    @PostMapping("/getDistance")
    public String getDistance(@RequestBody LocationServiceDTO locationServiceDTO) throws IOException,
            NoSuchAlgorithmException, InvalidKeyException {

        EmulationDTO dto = getGridAndWidth(locationServiceDTO);
        Double xCoord = getCorrectXCoord(locationServiceDTO.getXCoord(), Integer.valueOf(locationServiceDTO.getDeck()));
        Double xCoord2 = locationServiceDTO.getXCoord2() != null?
                getCorrectXCoord(locationServiceDTO.getXCoord(), Integer.valueOf(locationServiceDTO.getDeck())): null;

        Double yCoord = getCorrectYCoord(locationServiceDTO.getYCoord(), Integer.valueOf(locationServiceDTO.getDeck()));
        Double yCoord2 = locationServiceDTO.getYCoord2() != null?
                getCorrectXCoord(locationServiceDTO.getXCoord(), Integer.valueOf(locationServiceDTO.getDeck())): null;
        double scale = locationDataUtils.calculateScale(dto.getRealX(), dto.getGrid().length);
        Double gridX = xCoord / scale;
        Double gridY = yCoord / scale;

        Double gridX2 = locationServiceDTO.getXCoord2() != null? xCoord2 / scale : 0;
        Double gridY2 = locationServiceDTO.getYCoord2() != null? yCoord2 / scale : 0;

        dto.setIsDistance(true);

        Integer distance = 0;
        if(locationServiceDTO.getXCoord2() != null && locationServiceDTO.getYCoord2() != null){
            log.info("start Location x :{}, y :{} , end location x :{}, y :{}", gridX.intValue(), gridY.intValue(),
                    gridX2.intValue(), gridY2.intValue());
            distance = pathingService.minDistance(dto.getGrid(), gridX.intValue(),
                    gridY.intValue(),
                    gridX2.intValue(),
                    gridY2.intValue(),
                    dto, false, Integer.valueOf(locationServiceDTO.getDeck()), "", "");
        } else {
            distance = pathingService.minDistance(dto.getGrid(), gridX.intValue(), gridY.intValue(),
                    null,
                    null,
                    dto, false, Integer.valueOf(locationServiceDTO.getDeck()), "", "");
        }

        return String.valueOf(distance * scale);
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

   /* @GetMapping("/getGfCapacity")
    public String getGfCapacity(@RequestParam("gfId") @Nullable String gfId){
        Map<String, Integer> gfCapacity = locationGenerationService.gfCapacity;
        if(gfId != null && !"".equals(gfId)) return String.valueOf(gfCapacity.get(gfId));
        return gfCapacity.toString();
    }*/

    @GetMapping("/getGfCapacity")
    public List<GeofenceAttributes> getGfCapacity(@RequestParam("gfId") @Nullable String gfId){
        Map<String, Integer> gfCapacity = locationGenerationService.gfCapacity;
        Map<String, GeofenceAttributes> gfAttributesMap = LocationDataUtils.gfAttr;

        List<GeofenceAttributes> gfAttrList = new ArrayList<>();
        if(gfId != null && !"".equals(gfId)) {
            GeofenceAttributes singleGf = gfAttributesMap.get(gfId);
            singleGf.setCapacity(gfCapacity.get(gfId));
            gfAttrList.add(singleGf);
        } else {
            gfAttributesMap.forEach((k, v) -> {
                v.setCapacity(gfCapacity.get(k) == null ? 0 : gfCapacity.get(k));
                gfAttrList.add(v);
            });
        }
        return gfAttrList;
    }

    @GetMapping("/getGfSpace")
    public String getGfSpace(@RequestParam("gfId") @Nullable String gfId){
        Map<String, Integer> gfSpace = LocationDataUtils.gfSpace;
        if(gfId != null && !"".equals(gfId)) return String.valueOf(gfSpace.get(gfId));
        return gfSpace.toString();
    }

    @PostMapping("/runEmulation")
    public void runEmulation(@RequestBody EmulationDTO dto) throws IOException, InterruptedException {

        String[][] deck7 = mappingService.convertDeck7ToColorArray();
        String[][] deck8 = mappingService.convertDeck8ToColorArray();
        String[][] deck9 = mappingService.convertDeck9ToColorArray();

        Map<Integer, String[][]> decks = new HashMap<>();
        decks.put(7, deck7);
        decks.put(8, deck8);
        decks.put(9, deck9);

        String[][] grid = null;
        //dto.setDeck7Scale(locationDataUtils.calculateScale(dto.getRealX() == null? 143: dto.getRealX(), deck7.length));
        dto.setDeck7Scale(locationDataUtils.calculateScale( 150, deck7.length));
        dto.setDeck8Scale(locationDataUtils.calculateScale(65, deck8.length));
        dto.setDeck9Scale(locationDataUtils.calculateScale(65, deck9.length));

        dto.setPathErrorPrcntg(dto.getPathErrorPrcntg() == null? 3 : dto.getPathErrorPrcntg());
        dto.setPositionError(dto.getPositionError() == null? 0.5 : dto.getPositionError());
        //first wave of passengers
        dto.setAfterFirstWave(false);
        //format timestamp
        dto.setStartTimestamp(LocationDataUtils.dateToString(LocalDateTime.now()));
        log.info("start timestamp :{}", dto.getStartTimestamp());

        if(dto.getColorCodeList() != null && !dto.getColorCodeList().isEmpty()) {
            dto.populateNewGeofenceMap();
        }
        dto.setGeofences(dto.getGeofences() == null? LocationDataUtils.gfMap : dto.getGeofences());
        dto.setIsDistance(false);
        dto.setHasDelay(true);
        //generate faulty end location
        locationGenerationService.evictGfCapMap();
        for(int i=0; i<dto.getNoOfData(); i++){
            //set random deck as start if no specific deck has been set
            Integer deckNo = dto.getDeck() == null? random.ints(7, 10).findFirst().getAsInt() : dto.getDeck();

            grid = decks.get(deckNo);

            dto.setSpeed(locationDataUtils.getRandomSpeed());
            AtomicReference<Pair<Integer, Integer>> startLocation = new AtomicReference<>(locationDataUtils.generateRandomStartPoint(grid));
            AtomicReference<Boolean> isAfterFirst = new AtomicReference<>(false);

            String[][] finalGrid = grid;
            Integer finalDeckNo = deckNo;
            new Thread(() -> {
                try {

                    Pair<Integer, Integer> finalStartLocation = startLocation.get();

                    if (dto.getPathErrorPrcntg() > (int) (Math.random() * (100))){
                        Pair<Integer, Integer> endLocation = locationDataUtils.generateFaultyEndPoint(finalGrid);
                        log.info("start Location :{}, end location :{}", finalStartLocation, endLocation);
                        pathingService.minDistance(finalGrid, finalStartLocation.getLeft(),
                                finalStartLocation.getRight(),
                                endLocation.getLeft(),
                                endLocation.getRight(),
                                dto, isAfterFirst.get(), finalDeckNo, "", "");
                    } else {
                         pathingService.minDistance(finalGrid, finalStartLocation.getLeft(),
                                finalStartLocation.getRight(),
                                null,
                                null,
                                dto, isAfterFirst.get(), finalDeckNo, "", "");
                    }
                } catch (NoSuchAlgorithmException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }

            }).start();

        }
    }

    private EmulationDTO getGridAndWidth(LocationServiceDTO locationServiceDTO){
        EmulationDTO emulationDTO = new EmulationDTO();
        String[][] deck7 = mappingService.convertDeck7ToColorArray();
        String[][] deck8 = mappingService.convertDeck8ToColorArray();
        String[][] deck9 = mappingService.convertDeck9ToColorArray();
        switch (locationServiceDTO.getDeck()) {
            case "7":
                emulationDTO.setGrid(deck7);
                emulationDTO.setRealX(LocationDataUtils.widthsPerDeck.get(7));
                break;
            case "8":
                emulationDTO.setGrid(deck8);
                emulationDTO.setRealX(LocationDataUtils.widthsPerDeck.get(8));
                break;
            case "9":
                emulationDTO.setGrid(deck9);
                emulationDTO.setRealX(LocationDataUtils.widthsPerDeck.get(9));
                break;
        }
        return emulationDTO;
    }

    //if given x coordinate are bigger than the max then return the max x coordinate
    private Double getCorrectXCoord(String xCoord, Integer deckNo){
        Double deckOffset = deckNo != 7? 90D : 0;
        Double xCoordDbl = Double.parseDouble(xCoord) - deckOffset;
        return xCoordDbl > LocationDataUtils.widthsPerDeck.get(deckNo)? LocationDataUtils.widthsPerDeck.get(deckNo) - 0.2: xCoordDbl;
    }
    //if given y coordinate are bigger than the max then return the max y coordinate
    private Double getCorrectYCoord(String yCoord, Integer deckNo){
        Double yCoordDbl = Double.parseDouble(yCoord);
        return yCoordDbl > LocationDataUtils.heightsPerDeck.get(deckNo)? LocationDataUtils.heightsPerDeck.get(deckNo) - 0.2 : yCoordDbl;
    }

}
