package gr.uaegean.location.emulation.util;

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
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static String exitVal = "#00AFFF";

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

    public static void setExitVal(String newExitVal){
        exitVal = newExitVal;
    }

    public static void setGeofence(String color, String name){
        gfMap.put(color, name);
    }

}
