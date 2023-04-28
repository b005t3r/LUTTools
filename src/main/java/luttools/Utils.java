package luttools;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Objects;

public final class Utils {
    public static BufferedImage createLutTemplateImage() {
        BufferedImage image     = new BufferedImage(256 * 16, 256 * 16, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster   = image.getRaster();

        int[] color = new int[3];

        for(int r = 0; r < 256; ++r) {
            for(int g = 0; g < 256; ++g) {
                for(int b = 0; b < 256; ++b) {
                    int x = r % 16 + 16 * g;
                    int y = r / 16 + 16 * b;

                    color[0] = r;
                    color[1] = g;
                    color[2] = b;

                    raster.setPixel(x, y, color);
                }
            }
        }

        return image;
    }

    public static final class ColorMapping {
        int r;
        int g;
        int b;

        public ColorMapping(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ColorMapping)) return false;
            ColorMapping that = (ColorMapping) o;
            return r == that.r && g == that.g && b == that.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b);
        }
    }

    public static ColorMapping[][][] extractLutData(BufferedImage baseline, BufferedImage corrected) {
        if(baseline.getWidth() != corrected.getWidth() || baseline.getHeight() != corrected.getHeight())
            throw new IllegalArgumentException("baseline and corrected have to have the same size");

        ColorMapping[][][] result = new ColorMapping[256][256][256];
        for(int r = 0; r < 256; ++r)
            for(int g = 0; g < 256; ++g)
                for(int b = 0; b < 256; ++b)
                    result[r][g][b] = null;

        WritableRaster baselineRaster   = baseline.getRaster();
        WritableRaster correctedRaster  = corrected.getRaster();

        int[] baselineColor     = new int[3];
        int[] correctedColor    = new int[3];

        for(int x = 0; x < baseline.getWidth(); ++x) {
            for(int y = 0; y < baseline.getHeight(); ++y) {
                baselineRaster.getPixel(x, y, baselineColor);
                correctedRaster.getPixel(x, y, correctedColor);

                int r = baselineColor[0];
                int g = baselineColor[1];
                int b = baselineColor[2];

                result[r][g][b] = new ColorMapping(correctedColor[0], correctedColor[1], correctedColor[2]);
            }
        }

        return result;
    }
}
