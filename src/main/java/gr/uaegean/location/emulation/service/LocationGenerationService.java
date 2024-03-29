package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.*;
import gr.uaegean.location.emulation.model.entity.LocationTO;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gr.uaegean.location.emulation.util.LocationDataUtils.*;

@Slf4j
@Service
public class LocationGenerationService {

    private LocationDataService locationDataService;

    @Autowired
    LocationGenerationService(LocationDataService locationDataService){
        this.locationDataService = locationDataService;
    }

    public ConcurrentHashMap<String, Integer> gfCapacity = new ConcurrentHashMap<>();

    public LocationTO generateLocationData(Deque<Pair<Integer,Integer>> route, String[][] grid,
                                     EmulationDTO dto, Integer deckNo,
                                     LocationDTO locationDto) throws NoSuchAlgorithmException, InvalidKeyException {

        Map<String, String> geofences = dto.getGeofences().isEmpty()? LocationDataUtils.gfMap : dto.getGeofences();

        LocationTO locationData = new LocationTO();
        LocalDateTime previousPostTime = LocalDateTime.now();

        String macAddress = locationDto.getLocationTO().getMacAddress();
        String hashedMacAddress = locationDto.getLocationTO().getHashedMacAddress();

        double dwellTime = 0;
        Integer timeIncrement = 20;
        Double defaultSpeed = calculateDefaultSpeed();

        Pair<Integer,Integer> startCoords = route.getFirst();
        String prevIdxGf = grid[startCoords.getLeft()][startCoords.getRight()];

        //LocationDTO locationDto = new LocationDTO();
        //this is a new person initially set to true

        locationDto.setLocationTO(locationData);
        //add geofence at start

        locationData.setGeofence(populateGeofence(prevIdxGf,
                geofences.get(prevIdxGf),
                "ZONE_IN", locationData.getMacAddress(), locationData.getHashedMacAddress(),
                Double.valueOf(0), LocalDateTime.now(), deckNo));
        gfCapIncrease(prevIdxGf);
        //emulate delay in taking action in the beginning
        if(locationDto.getIsNewPerson()){
            LocationDataUtils.generateLocationAddress(locationData);
            macAddress = locationData.getMacAddress();
            hashedMacAddress = locationData.getHashedMacAddress();
            locationDto.setIsNewPerson(true);
            Long activationDelay = Long.valueOf(LocationDataUtils.getRandomActivationTime());
            log.info("delay in taking action :{}", activationDelay);
            LocalDateTime activationTime = LocalDateTime.now().plusSeconds(activationDelay);

            while(LocalDateTime.now().isBefore(activationTime)){

                locationData.setLocation(populateLocation(prevIdxGf,  startCoords, dto,
                        locationData.getHashedMacAddress(), LocalDateTime.now(), deckNo));

                dwellTime = dwellTime + timeIncrement;
                generateHeartAndOxygenLevels(locationDto, locationData);
                locationDto.setLocationTO(locationData);

                locationDataService.sendLocationData(locationDto);
                //locationData.setGeofence(new UserGeofenceUnit());
                //after first entry set new person to false
                locationDto.setIsNewPerson(false);

                //for real time run dto.hasDelay = true
                setDelay(dto, timeIncrement);
            }
            locationDto.setIsNewPerson(false);
        }
        locationData.setMacAddress(macAddress);
        locationData.setHashedMacAddress(hashedMacAddress);
        locationDto.setIsNewPerson(false);
        Iterator routeIterator = route.iterator();
        while(routeIterator.hasNext()){

            //Location location = new Location();
            Pair<Integer,Integer> coords = route.pop();
            String currentGf = grid[coords.getLeft()][coords.getRight()];
            //emulate geofence event
            if(!prevIdxGf.equals(currentGf)){
                if(geofences.get(prevIdxGf) != null){
                    //previous geofence exit
                    gfCapDecrease(prevIdxGf);
                    locationData.setGeofence(populateGeofence(prevIdxGf, geofences.get(prevIdxGf),
                            "ZONE_OUT", locationData.getMacAddress(),
                            locationData.getHashedMacAddress(),  dwellTime, LocalDateTime.now(), deckNo));
                    locationData.setLocation(populateLocation(currentGf, coords,  dto,
                            locationData.getHashedMacAddress(),  LocalDateTime.now(), deckNo));
                    generateHeartAndOxygenLevels(locationDto, locationData);
                    locationDto.setLocationTO(locationData);
                    locationDataService.sendLocationData(locationDto);
                }

                if(geofences.get(currentGf) != null){
                    //new geofence enter
                    gfCapIncrease(currentGf);
                    locationData.setGeofence(populateGeofence(currentGf,
                            geofences.get(grid[coords.getLeft()][coords.getRight()]),
                            "ZONE_IN", locationData.getMacAddress(), locationData.getHashedMacAddress(),
                            Double.valueOf(0), LocalDateTime.now(), deckNo));
                    locationData.setLocation(populateLocation(currentGf, coords,  dto,
                            locationData.getHashedMacAddress(),  LocalDateTime.now(), deckNo));
                    generateHeartAndOxygenLevels(locationDto, locationData);
                    locationDto.setLocationTO(locationData);
                    locationDataService.sendLocationData(locationDto);
                }

                if(locationDto.getLocationTO().getGeofence().getGfName().equalsIgnoreCase("muster station")){
                    break;
                }

                dwellTime = 0;
            } else {
                //calculate speed (time to travel each pixel)
                Long msPerPixel = LocationDataUtils.calculateMsPerPixel(dto, currentGf, gfCapacity.get(currentGf), deckNo, defaultSpeed);
                dwellTime = dwellTime + msPerPixel;
                try {
                    if(dto.getHasDelay()){
                        Thread.sleep(msPerPixel);
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }

            //emulate location
            if(LocalDateTime.now().isAfter(previousPostTime.plusSeconds(20)) || route.size() == 0){
                previousPostTime = LocalDateTime.now();
                locationData.setLocation(populateLocation(currentGf, coords,  dto,
                        locationData.getHashedMacAddress(), LocalDateTime.now(), deckNo));
                generateHeartAndOxygenLevels(locationDto, locationData);
                locationDto.setLocationTO(locationData);
                //locationData.setGeofence(new UserGeofenceUnit());
                locationDataService.sendLocationData(locationDto);
                locationDto.setIsNewPerson(false);
                if(route.size() == 0 && deckNo != 7){
                    gfCapDecrease(currentGf);
                }
            }

            //if route does not end in muster station then generate location for 5 more minutes
            if(route.size() == 0
                    && !exitVal.get(7).contains(currentGf)
                    && !exitVal.get(8).contains(currentGf)
                    && !exitVal.get(9).contains(currentGf)){
                LocalDateTime endTime = LocalDateTime.now().plusMinutes(5);
                while(LocalDateTime.now().isBefore(endTime)){
                    locationDataService.sendLocationData(locationDto);
                    setDelay(dto, 20);
                }
            }
            prevIdxGf = grid[coords.getLeft()][coords.getRight()];

        }

        return locationData;
    }

    private static UserLocationUnit populateLocation(String startGf, Pair<Integer, Integer> coords, EmulationDTO dto,
                                             String hashedMacAddress, LocalDateTime currentTimestamp,
                                             Integer deckNo){
        Double scale = dto.getDeck7Scale();
        switch(deckNo){
            case 8:
                scale = dto.getDeck8Scale();
                break;
            case 9:
                scale = dto.getDeck9Scale();
                break;
        }
        UserLocationUnit location = new UserLocationUnit();
        location.setGeofenceId(startGf);
        location.setGeofenceNames(Arrays.asList(dto.getGeofences().isEmpty()?
                LocationDataUtils.gfMap.get(startGf) : dto.getGeofences().get(startGf)));
        location.calculateCoords(coords, scale, dto.getPositionError(), deckNo);
        location.setTimestamp(LocationDataUtils.dateToString(currentTimestamp));
        location.setIsAssociated("true");
        location.setHashedMacAddress(hashedMacAddress);
        location.setBuildingId("shipId");
        location.setCampusId("campusId");
        location.setFloorId(String.valueOf(deckNo));
        location.setErrorLevel(String.valueOf(dto.getPositionError()));

        return location;
    }

    private static UserGeofenceUnit populateGeofence(String gfId, String gfName, String gfEvent,
                                                     String macAddress, String hashedMacAddress,
                                                     Double dwellTime, LocalDateTime currentTimestamp,
                                                     Integer deckNo){
        UserGeofenceUnit geofence = new UserGeofenceUnit();

        geofence.setGfId(gfId);
        geofence.setGfName(gfName);
        geofence.setGfEvent(gfEvent);
        geofence.setMacAddress(macAddress);
        geofence.setHashedMacAddress(hashedMacAddress);
        geofence.setDwellTime(String.valueOf(dwellTime));
        geofence.setTimestamp(LocationDataUtils.dateToString(currentTimestamp));
        geofence.setIsAssociated("true");
        geofence.setDeck(String.valueOf(deckNo));

        return geofence;
    }

    private void generateHeartAndOxygenLevels(LocationDTO locationDto, LocationTO locationTo){
        Integer oxygenSaturation = locationDto.getHasOxygenProblem()?
                generateOxygenSaturation(true): generateOxygenSaturation(false);
        Integer heartBeat = locationDto.getHasHeartProblem()?
                generateHeartBeat(true): generateHeartBeat(false);
        locationTo.setSaturation(String.valueOf(oxygenSaturation));
        locationTo.setHeartBeat(String.valueOf(heartBeat));
    }

    private void setDelay(EmulationDTO dto, Integer timeIncrement){
        if(dto.getHasDelay().equals(Boolean.TRUE)){
            try {
                Thread.sleep((long) timeIncrement * 1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void gfCapIncrease(String gfId){
        Integer count = gfCapacity.get(gfId);
        if(count == null) count = 0;
        count++;
        gfCapacity.put(gfId, count);
    }

    private void gfCapDecrease(String gfId){
        Integer count = gfCapacity.get(gfId);
        count--;
        gfCapacity.put(gfId, count);
    }

    public void evictGfCapMap(){
        gfCapacity.clear();
    }

    public Map<String, Integer> getGfCapMap(){
        return gfCapacity;
    }

    public void generateSingleLocation(String[][] grid, Pair<Integer,Integer> startCoords, Integer deckNo,
                                       EmulationDTO dto) throws NoSuchAlgorithmException, InvalidKeyException {
        String prevIdxGf = grid[startCoords.getLeft()][startCoords.getRight()];
        LocationTO locationData = new LocationTO();

        LocationDTO locationDto = new LocationDTO();

        locationDto.setLocationTO(locationData);

        Map<String, String> geofences = LocationDataUtils.gfMap;
        locationData.setGeofence(populateGeofence(prevIdxGf,
                geofences.get(prevIdxGf),
                "ZONE_IN", locationData.getMacAddress(), locationData.getHashedMacAddress(),
                Double.valueOf(0), LocalDateTime.now(), deckNo));
        gfCapIncrease(prevIdxGf);
        LocationDataUtils.generateLocationAddress(locationData);
        locationDto.setIsNewPerson(true);

        dto.setGeofences(LocationDataUtils.gfMap);

        //set oxygen  and heart beat rate at problems at first
        locationDto.setHasOxygenProblem(dto.getOxygenProblemPrnctg() != null
                && dto.getOxygenProblemPrnctg() > (int) (Math.random() * (100)));
        locationDto.setHasHeartProblem(dto.getHeartProblemPrnctg() != null
                && dto.getHeartProblemPrnctg() > (int) (Math.random() * (100)));

        locationData.setLocation(populateLocation(prevIdxGf,  startCoords, dto,
                    locationData.getHashedMacAddress(), LocalDateTime.now(), deckNo));

        generateHeartAndOxygenLevels(locationDto, locationData);
        locationDto.setLocationTO(locationData);

        locationDataService.sendLocationData(locationDto);

    }

}
