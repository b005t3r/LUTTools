package luttools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedImage template = Utils.createLutTemplateImage();

        //ImageIO.write(template, "png", new File(System.getProperty("user.home"), "lut_template.png"));

        BufferedImage baseline  = ImageIO.read(new File(System.getProperty("user.home"), "lut_template.png"));
        BufferedImage corrected = ImageIO.read(new File(System.getProperty("user.home"), "lut_ektar25.png"));

        Utils.ColorMapping[][][] lutFromTemplate = Utils.extractLutData(template, corrected);
        Utils.ColorMapping[][][] lutFromBaseline = Utils.extractLutData(baseline, corrected);

        System.out.println(Arrays.deepEquals(lutFromBaseline, lutFromTemplate));
    }
}
