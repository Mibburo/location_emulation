package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.*;
import gr.uaegean.location.emulation.model.entity.LocationData;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class PathingService {

    @Autowired
    private LocationDataService locationDataService;

    public Integer minDistance(String[][] grid, int startX, int startY, Integer endX, Integer endY, EmulationDTO dto) throws NoSuchAlgorithmException, InvalidKeyException {
        QItem source = new QItem(startX, startY, 0);
        String exitVal = dto.getEndGf() == null || "".equals(dto.getEndGf()) ? LocationDataUtils.exitVal : dto.getEndGf();

        log.info("start BFS");
        // applying BFS on matrix cells starting from source
        Queue<QItem> queue = new LinkedList<>();
        queue.add(new QItem(source.getRow(), source.getCol(), 0));

        boolean[][] visited = new boolean[grid.length][grid[0].length];
        visited[source.getRow()][source.getCol()] = true;
        String parent = "";
        Map<String, String> parentMap = new LinkedHashMap<>();
        Deque<Pair<Integer, Integer>> route = new LinkedList<>();
        String startPoint = startX +"-"+ startY;
        while (!queue.isEmpty()) {

            QItem p = queue.remove();
            if("".equals(parent)){
                parent = startPoint;
            } else {
                parentMap.put(p.getRow() +"-"+ p.getCol(), p.getParent());
                parent = p.getRow() +"-"+ p.getCol();
            }

            // Destination found;
            // faulty destination, enters only when faulty destination exists
            if(endX != null && endY !=null){
                if (p.getRow() == endX && p.getCol() == endY) {
                    destinationFound(grid, p,  parentMap,  route,  startPoint,  parent, dto);
                    log.info("end BFS");
                    return p.getDist();
                }
            } else {
                if (grid[p.getRow()][p.getCol()].equals(exitVal)) {
                    if(!dto.getIsDistance()){
                        destinationFound(grid, p, parentMap, route, startPoint, parent, dto);
                        log.info("end BFS");
                    }
                    return p.getDist();
                }
            }

            // moving up
            if (isValid(p.getRow() - 1, p.getCol(), grid, visited)) {
                queue.add(new QItem(p.getRow() - 1, p.getCol(),
                        p.getDist() + 1, parent));
                visited[p.getRow() - 1][p.getCol()] = true;
            }

            //moving up-left
            /*if (isValid(p.getRow() - 1, p.getCol()-1, grid, visited)) {
                queue.add(new QItem(p.getRow() - 1, p.getCol()-1,
                        p.getDist() + 1, parent));
                visited[p.getRow() - 1][p.getCol()-1] = true;
            }*/

            //moving up-right
            /*if (isValid(p.getRow() - 1, p.getCol()+1, grid, visited)) {
                queue.add(new QItem(p.getRow() - 1, p.getCol()+1,
                        p.getDist() + 1, parent));
                visited[p.getRow() - 1][p.getCol()+1] = true;
            }*/

            // moving down
            if (isValid(p.getRow() + 1, p.getCol(), grid, visited)) {
                queue.add(new QItem(p.getRow() + 1, p.getCol(),
                        p.getDist() + 1, parent));
                visited[p.getRow() + 1][p.getCol()] = true;
            }

            //moving down-left
            /*if (isValid(p.getRow() + 1, p.getCol()-1, grid, visited)) {
                queue.add(new QItem(p.getRow() + 1, p.getCol()-1,
                        p.getDist() + 1, parent));
                visited[p.getRow() + 1][p.getCol()-1] = true;
            }*/

            //moving down-right
            /*if (isValid(p.getRow() + 1, p.getCol()+1, grid, visited)) {
                queue.add(new QItem(p.getRow() + 1, p.getCol()+1,
                        p.getDist() + 1, parent));
                visited[p.getRow() + 1][p.getCol()+1] = true;
            }*/

            // moving left
            if (isValid(p.getRow(), p.getCol() - 1, grid, visited)) {
                queue.add(new QItem(p.getRow(), p.getCol() - 1,
                        p.getDist() + 1, parent));
                visited[p.getRow()][p.getCol() - 1] = true;
            }

            // moving right
            if (isValid(p.getRow(), p.getCol() + 1, grid,
                    visited)) {
                queue.add(new QItem(p.getRow(), p.getCol() + 1,
                        p.getDist() + 1, parent));
                visited[p.getRow()][p.getCol() + 1] = true;
            }
        }
        //return grid;
        return -1;
    }

    // checking if it's valid or not, checking walls and already visited nodes
    private static boolean isValid(int x, int y, String[][] grid, boolean[][] visited){
        if (x >= 0 && y >= 0 && x < grid.length
                && y < grid[0].length && !LocationDataUtils.walls.contains(grid[x][y])
                && visited[x][y] == false) {
            return true;
        }
        return false;
    }

    //adds the traversed route to a stack by getting the parent of each node (map(node->parent)) starting from the end
    private static Deque<Pair<Integer,Integer>> findPath(Map<String, String> nodes, String entryVal, String startPoint, Deque<Pair<Integer,Integer>> route){
        if(entryVal.equals(startPoint)){
            return route;
        } else {
            String[] node = nodes.get(entryVal).split("-");
            Pair<Integer, Integer> coords = new ImmutablePair<>(Integer.valueOf(node[0]), Integer.valueOf(node[1]));
            route.push(coords);
            return findPath(nodes, nodes.get(entryVal), startPoint, route);
        }
    }

    private void generateLocationData(Deque<Pair<Integer,Integer>> route, String[][] grid, EmulationDTO dto) throws NoSuchAlgorithmException, InvalidKeyException {

        Map<String, String> geofences = dto.getGeofences().isEmpty()? LocationDataUtils.gfMap : dto.getGeofences();

        LocationData locationData = LocationDataUtils.generateLocationAddress();

        LocalDateTime startTime = LocationDataUtils.stringToLDT(dto.getStartTimestamp());
        Long activationDelay = Long.valueOf(LocationDataUtils.getRandomActivationTime());
        log.info("delay in taking action :{}", activationDelay);
        LocalDateTime activationTime = startTime.plusSeconds(activationDelay);
        LocalDateTime currentTimestamp = startTime;

        double dwellTime = 0;
        Integer timeIncrement = 0;

        Pair<Integer,Integer> startCoords = route.getFirst();
        String prevIdxGf = grid[startCoords.getLeft()][startCoords.getRight()];

        LocationDTO locationDto = new LocationDTO();
        //this is a new person initially set to true
        locationDto.setIsNewPerson(true);
        locationDto.setLocationData(locationData);
        //add geofence at start
        locationData.setGeofence(populateGeofence(prevIdxGf,
                geofences.get(prevIdxGf),
                "ZONE_IN", locationData.getMacAddress(), locationData.getHashedMacAddress(),
                Double.valueOf(0), currentTimestamp));
        //emulate delay in taking action in the beginning
        while(currentTimestamp.isBefore(activationTime)){

            locationData.setLocation(populateLocation(prevIdxGf,  startCoords, dto, locationData.getHashedMacAddress(), currentTimestamp));
            timeIncrement = LocationDataUtils.getRandomTimeIncrements();
            dwellTime = dwellTime + timeIncrement;
            currentTimestamp = currentTimestamp.plusSeconds(Long.valueOf(timeIncrement));
            locationDto.setLocationData(locationData);
            locationDataService.sendLocationData(locationDto);
            //after first entry set new person to false
            locationDto.setIsNewPerson(false);

            //for real time run dto.hasDelay = true
            setDelay(dto, timeIncrement);

        }
        Iterator routeIterator = route.iterator();

        Integer move = 0;

        while(routeIterator.hasNext()){
            Location location = new Location();
            //timeIncrement = LocationDataUtils.getRandomTimeIncrements();
            Pair<Integer,Integer> coords = route.pop();
            //location.calculateCoords(coords, dto.getScale(), dto.getPositionError());

            //emulate geofence event
            if(!prevIdxGf.equals(grid[coords.getLeft()][coords.getRight()])){
                if(geofences.get(prevIdxGf) != null){
                    //previous geofence exit
                    locationData.setGeofence(populateGeofence(prevIdxGf, geofences.get(prevIdxGf),
                            "ZONE_OUT", locationData.getMacAddress(),
                            locationData.getHashedMacAddress(),  dwellTime, currentTimestamp));
                    locationData.setLocation(populateLocation(grid[coords.getLeft()][coords.getRight()], coords,  dto, locationData.getHashedMacAddress(),  currentTimestamp));
                    locationDto.setLocationData(locationData);

                    locationDataService.sendLocationData(locationDto);
                }

                if(geofences.get(grid[coords.getLeft()][coords.getRight()]) != null){
                    //new geofence enter
                    locationData.setGeofence(populateGeofence(grid[coords.getLeft()][coords.getRight()],
                            geofences.get(grid[coords.getLeft()][coords.getRight()]),
                            "ZONE_IN", locationData.getMacAddress(), locationData.getHashedMacAddress(),
                            Double.valueOf(0), currentTimestamp));
                    locationData.setLocation(populateLocation(grid[coords.getLeft()][coords.getRight()], coords,  dto, locationData.getHashedMacAddress(),  currentTimestamp));
                    locationDto.setLocationData(locationData);

                    locationDataService.sendLocationData(locationDto);
                }

                if(locationDto.getLocationData().getGeofence().getGfName().equalsIgnoreCase("muster station")){
                    break;
                }

                dwellTime = 0;
            } else {
                dwellTime = dwellTime + 0.1;
                currentTimestamp = currentTimestamp.plusNanos(LocationDataUtils.getNanoIncrementsInRange() * 100);
                try {
                    if(dto.getHasDelay()){
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }

            //emulate location
            if(move % dto.getSpeed() == 0 || route.size() == 0){
                timeIncrement = LocationDataUtils.getRandomTimeIncrements();
                currentTimestamp = currentTimestamp.plusSeconds(Long.valueOf(timeIncrement));
                locationData.setLocation(populateLocation(grid[coords.getLeft()][coords.getRight()], coords,  dto, locationData.getHashedMacAddress(),  currentTimestamp));
                locationDto.setLocationData(locationData);

                locationDataService.sendLocationData(locationDto);
                locationDto.setIsNewPerson(false);
                //for real time run dto.hasDelay = true
                setDelay(dto, timeIncrement);
            }
            move++;
            prevIdxGf = grid[coords.getLeft()][coords.getRight()];

        }

    }

    private void destinationFound(String[][] grid, QItem p, Map<String, String> parentMap, Deque<Pair<Integer, Integer>> route, String startPoint, String parent, EmulationDTO dto ) throws NoSuchAlgorithmException, InvalidKeyException {
        Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(p.getRow(), p.getCol());
        route.add(pair);
        route = findPath(parentMap, parent, startPoint, route);
        generateLocationData(route, grid, dto);
    }

    private static Location populateLocation(String startGf, Pair<Integer, Integer> coords, EmulationDTO dto, String hashedMacAddress, LocalDateTime currentTimestamp){
        Location location = new Location();
        location.setGeofenceId(startGf);
        location.setGeofenceNames(Arrays.asList(dto.getGeofences().isEmpty()? LocationDataUtils.gfMap.get(startGf) : dto.getGeofences().get(startGf)));
        location.calculateCoords(coords, dto.getScale(), dto.getPositionError());
        location.setTimestamp(LocationDataUtils.dateToString(currentTimestamp));
        location.setIsAssociated("true");
        location.setHashedMacAddress(hashedMacAddress);
        location.setBuildingId("shipId");
        location.setCampusId("campusId");
        location.setFloorId("floorId");
        location.setErrorLevel(String.valueOf(dto.getPositionError()));

        return location;
    }

    private static Geofence populateGeofence(String gfId, String gfName, String gfEvent, String macAddress, String hashedMacAddress, Double dwellTime, LocalDateTime currentTimestamp){
        Geofence geofence = new Geofence();

        geofence.setGfId(gfId);
        geofence.setGfName(gfName);
        geofence.setGfEvent(gfEvent);
        geofence.setMacAddress(macAddress);
        geofence.setHashedMacAddress(hashedMacAddress);
        geofence.setDwellTime(String.valueOf(dwellTime));
        geofence.setTimestamp(LocationDataUtils.dateToString(currentTimestamp));
        geofence.setIsAssociated("true");

        return geofence;
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
}