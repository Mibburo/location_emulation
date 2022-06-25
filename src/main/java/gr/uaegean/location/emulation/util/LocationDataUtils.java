package gr.uaegean.location.emulation.util;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.model.GeofenceAttributes;
import gr.uaegean.location.emulation.model.entity.LocationTO;
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
    public static Map<String, GeofenceAttributes> gfAttr = new HashMap<>();

    //geofence id(hex color code) and name
    public static Map<String, String> gfMap = Stream.of(new String[][] {

            { "#FF00FF", "7DG1" },
            { "#324E65", "7DG2" },
            { "#18541D", "7DG3" },
            { "#FFFF00", "7DG4" },
            { "#5F0087", "7CG1" },
            { "#324EAD", "7CG2" },
            { "#87D700", "7BG1" },
            { "#875AA5", "7BG2" },
            { "#0000FF", "7BG3" },
            { "#FD648A", "7BG4" },
            { "#F8FD97", "7BG5" },
            { "#00AFFF", "7BG6, Muster Station" },
            { "#CADFFF", "S7-6.1" },
            { "#606F22", "S7-6.1-1-entry, us81-1" },
            { "#56A8C2", "S7-6.1-2-entry, us81-2" },
            { "#529A7B", "S7-6.2" },
            { "#70DAB3", "S7-6.2-entry, us82" },
            { "#E9CB72", "S7-6.3" },
            { "#EA2B64", "S7-6.3-entry, us83" },


            { "#00FFFF", "geofence 3" },
            { "#00AF00", "geofence 4" },
            { "#5F00FF", "geofence 8" },
            { "#5FAF5F", "geofence 9" },
            { "#5FFFAF", "geofence 10" },
            { "#FFAF00", "geofence 12" },
            { "#D5B742", "S8-7.2" },
            { "#4BB5CB", "S8-7.2-exit, exit82" },





            { "#FE52FE", "S9-8.2-exit, exit92" },
            { "#1F528F", "S8-7.2-entry, us92" },
            { "#858592", "S8-7.3" },
            { "#ED1C24", "S8-7.3-exit, exit83" },
            { "#0F1D79", "S9-8.3-exit, exit93" },
            { "#361788", "S8-7.3-entry, us93" },

            { "#77E1DF", "8BG1" },
            { "#CE5397", "8BG2" },
            { "#7092BE", "8BG3" },
            { "#6952C5", "8BG4" },
            { "#F4CC6C", "8BG5" },
            { "#3F48CC", "8BG6" },
            { "#22B14C", "8BG7" },
            { "#041E84", "8BG8" },
            { "#E74730", "8BG9" },
            { "#FFAEC9", "8BG10" },
            { "#C8BFE7", "S8-7.1" },
            { "#66F7C0", "S8-7.1-exit-1, exit81" },



            { "#867D3E", "S9-8.1-1, exit91-1" },
            { "#BB0B8F", "S8-7.1-entry-1, us91-1" },
            { "#2D7B15", "S9-8.1-2, exit91-2" },
            { "#A94B23", "S8-7.1-entry-2, us91-2" },

            { "#FF6F17", "S8-7.1-exit-2, exit81-2" },


            { "#A6FFB1", "S9-8.2" },
            { "#AD1414", "9BG2" },
            { "#E08752", "S9-8.3" },
            { "#F0F858", "S9-8.1" },
            { "#6F0266", "9BG1" },

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    //map of exit -> entry geofences from one deck to an other
    public static Map<String, String> gfEntry = Stream.of(new String[][] {
            { "#4BB5CB", "#70DAB3" },
            { "#ED1C24", "#EA2B64" },
            { "#66F7C0", "#606F22" },
            { "#0F1D79", "#361788" },
            { "#FE52FE", "#1F528F" },
            { "#867D3E", "#BB0B8F" },
            { "#2D7B15", "#A94B23" },
            { "#FF6F17", "#56A8C2" },

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

    public static LocationTO generateLocationAddress() throws NoSuchAlgorithmException, InvalidKeyException {
        LocationTO cl = new LocationTO();
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
        int x = (int) (Math.random() * (grid.length) - 1);
        int y = (int) (Math.random() * (grid[0].length) - 1);
        if(walls.contains(grid[x][y])){
            return generateRandomStartPoint(grid);
        }
        return new ImmutablePair<>(x, y);
    }

    public static Pair<Integer, Integer> getRandomStartPointInEntranceGf(String[][] grid, String gfId){
        GeofenceAttributes gfAttributes = gfAttr.get(gfEntry.get(gfId));
        int x = random.nextInt(gfAttributes.getXEnd()-gfAttributes.getXStart()) + gfAttributes.getXStart();
        //int x = (int) (Math.random() * (grid.length) - 1) ;
        int y = random.nextInt(gfAttributes.getYEnd()-gfAttributes.getYStart()) + gfAttributes.getYStart();
        //int y = (int) (Math.random() * (grid[0].length) - 1) ;
        if(!grid[x][y].equals(gfEntry.get(gfId))){
            return getRandomStartPointInEntranceGf(grid, gfId);
        }
        return new ImmutablePair<>(x, y);
    }

    public Pair<Integer, Integer> generateFaultyEndPoint(String[][] grid){
        int x = (int) (Math.random() * (grid.length) - 1);
        int y = (int) (Math.random() * (grid[0].length) - 1);
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
        return random.ints(0, 120).findFirst().getAsInt();
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
        //get the lower of the default speed or the new adjusted speed
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
