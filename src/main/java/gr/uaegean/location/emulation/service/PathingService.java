package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.*;
import gr.uaegean.location.emulation.model.entity.LocationTO;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
public class PathingService {

    private LocationDataService locationDataService;
    private LocationGenerationService locationGenerationService;
    private LocationDataUtils locationDataUtils;
    private MappingService mappingService;

    @Autowired
    PathingService(LocationDataService locationDataService, LocationGenerationService locationGenerationService,
                   MappingService mappingService){
        this.locationDataService = locationDataService;
        this.locationGenerationService = locationGenerationService;
        this.mappingService = mappingService;
    }

    public Integer minDistance(String[][] grid, int startX, int startY, Integer endX, Integer endY,
                               EmulationDTO dto, Integer deckNo,
                               LocationDTO locationDTO)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        log.info("start BFS");

        QItem source = new QItem(startX, startY, 0);
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
                    if(!dto.getIsDistance()) {
                        destinationFound(grid, p, parentMap, route, startPoint, parent,
                                dto, deckNo, grid[p.getRow()][p.getCol()],
                                true, locationDTO);
                        log.info("end BFS Faulty");
                    }
                    return p.getDist();
                }
            } else {
                if (LocationDataUtils.exitVal.get(deckNo).contains(grid[p.getRow()][p.getCol()])) {
                    if(!dto.getIsDistance()){
                        destinationFound(grid, p, parentMap, route, startPoint, parent,
                                dto, deckNo, grid[p.getRow()][p.getCol()],
                                false, locationDTO);
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
    private static Deque<Pair<Integer,Integer>> findPath(Map<String, String> nodes, String entryVal,
                                                         String startPoint, Deque<Pair<Integer,Integer>> route){
        if(entryVal.equals(startPoint)){
            return route;
        } else {
            String[] node = nodes.get(entryVal).split("-");
            Pair<Integer, Integer> coords = new ImmutablePair<>(Integer.valueOf(node[0]), Integer.valueOf(node[1]));
            route.push(coords);
            return findPath(nodes, nodes.get(entryVal), startPoint, route);
        }
    }

    private void destinationFound(String[][] grid, QItem p, Map<String, String> parentMap,
                                  Deque<Pair<Integer, Integer>> route, String startPoint, String parent,
                                  EmulationDTO dto, Integer deckNo, String exitGf,
                                  Boolean isFaultyDest, LocationDTO locationDTO )
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(p.getRow(), p.getCol());
        route.add(pair);
        route = findPath(parentMap, parent, startPoint, route);
        LocationTO location = locationGenerationService.generateLocationData(route, grid, dto, deckNo, locationDTO);
        locationDTO.getLocationTO().setMacAddress(location.getMacAddress());
        locationDTO.getLocationTO().setHashedMacAddress(location.getHashedMacAddress());
        if(!isFaultyDest) rerunPathfindingForNextDeck(deckNo, dto, exitGf, locationDTO);
    }

    private void rerunPathfindingForNextDeck(Integer deckNo, EmulationDTO dto, String exitGf, LocationDTO locationDTO){
        //if running deck is not deck 7 then get the next deck and rerun pathing
        // switched to run only for deck 8 after changing deck simulation 'if(deckNo != 7)' for default use
        if(deckNo == 8){
            deckNo--;
            String[][] deck = {};
            switch (deckNo){
                case 8:
                    deck = mappingService.convertDeck8ToColorArray();
                    break;
                case 7:
                    deck = mappingService.convertDeck7ToColorArray();
                    break;
            }
            //get start location in new deck where exitGf (old deck) == entranceGf in new deck
            Pair<Integer, Integer> startLocation = LocationDataUtils.getRandomStartPointInEntranceGf(deck, exitGf);
            try {
                minDistance(deck, startLocation.getLeft(),
                        startLocation.getRight(),
                        null,
                        null,
                        dto, deckNo,
                        locationDTO);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}