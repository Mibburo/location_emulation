package gr.uaegean.location.emulation.service;

import gr.uaegean.location.emulation.model.EmulationDTO;
import gr.uaegean.location.emulation.model.entity.LocationData;
import gr.uaegean.location.emulation.util.LocationDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
@Component
public class MappingService {



    /*public int[][] convertImageToArray() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("palaemon_ship_diagram_with_geofences.png").getFile()); // Be sure to read input file, you have error reading it.
        BufferedImage bufferedImage = ImageIO.read(file);
        WritableRaster wr = bufferedImage.getRaster();
        log.info("max x ? :{} ", wr.getWidth());
        log.info("max y ? :{} ", wr.getHeight());
        log.info("width :{} ", bufferedImage.getWidth());
        log.info("height :{} ", bufferedImage.getHeight());

        int[][] imageArray = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];

        for (int i = 0; i < wr.getWidth(); i++) {
            for (int j = 0; j < wr.getHeight(); j++) {
                int pixel = wr.getSample(i, j, 0); // the sample in the specified band for the pixel at the specified coordinate.
                imageArray[j][i] = pixel;
            }
        }

        return imageArray;
    }*/

    @EventListener(ApplicationReadyEvent.class)
    @Cacheable("deck7Map")
    public String[][] convertDeck7ToColorArray() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("static/images/deck7emu.png");
        return getImageArray(in);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Cacheable("deck8Map")
    public String[][] convertDeck8ToColorArray(){
        InputStream in = getClass().getClassLoader().getResourceAsStream("static/images/deck8emu.png");
        return getImageArray(in);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Cacheable("deck9Map")
    public String[][] convertDeck9ToColorArray(){
        InputStream in = getClass().getClassLoader().getResourceAsStream("static/images/deck9emu.png");
        return getImageArray(in);
    }

    //for visualization purposes
    public String[][] convertImageToInvertedArray(EmulationDTO dto) throws IOException {

        InputStream in = getClass().getResourceAsStream("static/images/deck8emu.png");

        BufferedImage bufferedImage = ImageIO.read(in);

        String[][] imageArray = new String[bufferedImage.getHeight()][bufferedImage.getWidth()];
        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                //int pixel = wr.getSample(i, j, 0); // the sample in the specified band for the pixel at the specified coordinate.
                Color c = new Color(bufferedImage.getRGB(i,j));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                String hex = String.format("#%02X%02X%02X", red, green, blue);
                imageArray[i][j] = hex;
            }
        }

        return imageArray;
    }

    private String[][] getImageArray(InputStream in){

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(in);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        String[][] imageArray = new String[bufferedImage.getWidth()][bufferedImage.getHeight()];
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                //int pixel = wr.getSample(i, j, 0); // the sample in the specified band for the pixel at the specified coordinate.
                Color c = new Color(bufferedImage.getRGB(i,j));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                String hex = String.format("#%02X%02X%02X", red, green, blue);
                imageArray[i][j] = hex;
                if(!hex.equals("#000000")){
                    Integer space = LocationDataUtils.gfSpace.get(hex) == null? 1 : LocationDataUtils.gfSpace.get(hex) + 1;
                    LocationDataUtils.gfSpace.put(hex, space);
                }
            }
        }
        log.info("cached map");
        return imageArray;
    }
}
