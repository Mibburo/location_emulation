package gr.uaegean.location.emulation.util;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.model.entity.LocationData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Service
public class LocationDataUtils {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public final static List<String> walls = List.of("#000000", "#010000", "#000100", "#000001");
    static Random random = new Random();

    public static Map<String, Integer> gfSpace = new HashMap<>();

    //geofence id(hex color code) and name
    public static Map<String, String> gfMap = Stream.of(new String[][] {
            { "#0000FF", "geofence 1" },
            { "#FF00FF", "geofence 2" },
            { "#00FFFF", "geofence 3" },
            { "#00AF00", "geofence 4" },
            { "#5F00FF", "geofence 5" },
            { "#FFFF00", "geofence 6" },
            { "#00AFFF", "Muster Station" },
            { "#5F0087", "geofence 8" },
            { "#5FAF5F", "geofence 9" },
            { "#5FFFAF", "geofence 10" },
            { "#87D700", "geofence 11" },
            { "#FFAF00", "geofence 12" },
            { "#3F48CC", "geofence 13" },
            { "#D5B742", "geofence 14" },
            { "#4BB5CB", "geofence 15, exit81" },
            { "#FE52FE", "geofence 16, us81, exit91" },
            { "#858592", "geofence 17" },
            { "#ED1C24", "geofence 18, exit82" },
            { "#0F1D79", "geofence 19, us82, exit92" },
            { "#E74730", "geofence 20" },
            { "#FFAEC9", "geofence 21" },
            { "#22B14C", "geofence 22" },
            { "#C8BFE7", "geofence 23" },
            { "#7092BE", "geofence 24" },
            { "#6952C5", "geofence 25" },
            { "#66F7C0", "geofence 26, exit83" },
            { "#867D3E", "geofence 27, us83, exit93" },
            { "#2D7B15", "geofence 28, us84, exit94" },
            { "#FF6F17", "geofence 29, exit84" },
            { "#A6FFB1", "geofence 30" },
            { "#E08752", "geofence 31" },
            { "#F0F858", "geofence 32" },
            { "#6F0266", "geofence 33" },
            { "#529A7B", "geofence 34" },
            { "#F8FD97", "geofence 35" },
            { "#CADFFF", "geofence 36" },
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static Map<Integer, Double> widthsPerDeck = Map.of(7, 155D,
            8, 60D,
            9, 60D );

    public static Map<Integer, Double> heightsPerDeck = Map.of(7, 34.1D,
            8, 35.8D,
            9, 33D );
    //public static List<String> exitVal = List.of("#00AFFF", "#4BB5CB", "#ED1C24","#66F7C0","#FF6F17");

    // exit geofences per deck number
    public static Map<Integer, List<String>> exitVal = Map.of(7, List.of("#00AFFF"),
            8, List.of("#4BB5CB", "#ED1C24","#66F7C0","#FF6F17"),
            9, List.of("#FE52FE", "#0F1D79", "#867D3E", "#2D7B15") );

    public static LocationData generateLocationAddress() throws NoSuchAlgorithmException, InvalidKeyException {
        LocationData cl = new LocationData();
        String macAddress = getRandomMacAddress();
        cl.setMacAddress(macAddress);
        cl.setHashedMacAddress(getHmacHash("HmacMD5", macAddress, "123456"));

        return cl;
    }

    public static String getRandomMacAddress() {
        String mac = "";
        Random r = new Random();
        for (int i = 0; i < 6; i++) {
            int n = r.nextInt(255);
            if(i == 5){
                mac += String.format("%02x", n);
            } else {
                mac += String.format("%02x:", n);
            }
        }
        return mac.toUpperCase();
    }

    public static String getHmacHash(String algorithm, String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return bytesToHex(mac.doFinal(data.getBytes()));
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte h : hash) {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public Pair<Integer, Integer> generateRandomStartPoint(String[][] grid){
        int x = (int) (Math.random() * (grid.length));
        int y = (int) (Math.random() * (grid[0].length));
        if(walls.contains(grid[x][y])){
            return generateRandomStartPoint(grid);
        }
        return new ImmutablePair<>(x, y);
    }

    public static Pair<Integer, Integer> getRandomStartPointInEntranceGf(String[][] grid, String gfId){
        int x = (int) (Math.random() * (grid.length));
        int y = (int) (Math.random() * (grid[0].length));
        if(!grid[x][y].equals(gfId)){
            return getRandomStartPointInEntranceGf(grid, gfId);
        }
        return new ImmutablePair<>(x, y);
    }

    public Pair<Integer, Integer> generateFaultyEndPoint(String[][] grid){
        int x = (int) (Math.random() * (grid.length));
        int y = (int) (Math.random() * (grid[0].length));
        if(walls.contains(grid[x][y]) || grid[x][y].equals(exitVal)){
            return generateFaultyEndPoint(grid);
        }
        return new ImmutablePair<>(x, y);
    }

    public double calculateScale(double actualWidth, int gridWidth){
        return actualWidth/gridWidth;
    }

    public static long calculateMsPerPixel(EmulationDTO dto, String gfId, Integer capacity, Integer deckNo, Double defaultSpeed){
        Double scale = getScaleByDeck(dto, deckNo);
        Double pixelsPerSec = calculateAdjustedSpeed(defaultSpeed, gfId, capacity, dto, deckNo)/scale;
        Double ms = 1000/pixelsPerSec;
        return ms.longValue();
    }

    public static Integer getRandomSpeed(){
        //Random random = new Random();
        return random.ints(120, 180).findFirst().getAsInt();
    }

    public static Integer getRandomTimeIncrements(){
        //Random random = new Random();
        return random.ints(15, 30).findFirst().getAsInt();
    }

    public static Long getNanoIncrementsInRange(){
        return Long.valueOf(random.ints(800000, 1200000).findFirst().getAsInt());
    }

    public static Integer getRandomActivationTime(){
        //Random random = new Random();
        return random.ints(0, 180).findFirst().getAsInt();
    }

    public static LocalDateTime stringToLDT(String dateS) {
        return LocalDateTime.parse(dateS, formatter);
    }

    public static String dateToString(LocalDateTime ldt) {
        return ldt.format(formatter);
    }

    /*public static void setExitVal(String newExitVal){
        exitVal = newExitVal;
    }*/

    public static void setGeofence(String color, String name){
        gfMap.put(color, name);
    }

    public static double calculateDefaultSpeed(){
        return random.doubles(0.3, 1.0).findFirst().getAsDouble();
    }

    public static double calculateAdjustedSpeed(Double defaultSpeed, String gfId, Integer capacity, EmulationDTO dto, Integer deckNo){

        Double adjustedSpeed = defaultSpeed;
        Double density = calculateGfDensity(capacity, gfId, dto, deckNo);
        if(density >= 1.9 && density < 3.2){
            adjustedSpeed = 0.67;
        }
        if(density >= 3.2 && density < 3.5) {
            adjustedSpeed = 0.2;
        }
        if(density >= 3.5) {
            adjustedSpeed = 0.1;
        }
        adjustedSpeed = defaultSpeed<adjustedSpeed? defaultSpeed : adjustedSpeed;
        return adjustedSpeed;
    }

    private static Double calculateGfDensity(Integer capacity, String gfId, EmulationDTO dto, Integer deckNo){
        Integer pixelSpace = gfSpace.get(gfId);
        Double scale = getScaleByDeck(dto, deckNo);

        Double spaceM2 = pixelSpace * Math.pow(scale, 2);
        return capacity/spaceM2;
    }

    private static Double getScaleByDeck(EmulationDTO dto, Integer deckNo){
        Double scale = null;
        switch (deckNo){
            case 7:
                scale = dto.getDeck7Scale();
                break;
            case 8:
                scale = dto.getDeck8Scale();
                break;
            case 9:
                scale = dto.getDeck9Scale();
                break;
        }

        return scale;
    }

}
