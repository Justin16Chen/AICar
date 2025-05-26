package aicar.utils.drawing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageModifier {

    public static BufferedImage replacePixels(BufferedImage img, Color targetColor, Color replacementColor) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int targetRGB = targetColor.getRGB();
        int replacementRGB = replacementColor.getRGB();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                if (pixel == targetRGB) {
                    newImage.setRGB(x, y, replacementRGB);
                } else {
                    newImage.setRGB(x, y, pixel);
                }
            }
        }
        return newImage;
    }
}
