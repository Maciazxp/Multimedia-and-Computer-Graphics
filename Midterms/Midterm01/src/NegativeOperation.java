import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Class for operation 2: APPLY NEGATIVE
// Inverts the color of each pixel inside the region on the working image.
public class NegativeOperation implements ImageOperation {

    @Override
    public BufferedImage apply(BufferedImage img, Region region) throws IOException {

        for (int y = region.getMinY(); y <= region.getMaxY(); y++) {  //scan the image from the bounding box
            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                if (region.isPointInside(x, y)) { //call the method isPointInside
                    //if the pixel is inside
                    int rgbOriginal = img.getRGB(x, y); //Read the original RGB value of the pixel at coordinates (x, y) (getRGB() returns a single integer containing the packed ARGB color
                    Color c = new Color(rgbOriginal); //Convert the packed integer color into a Color object

                    //Extract each color channel and compute its negative value.
                    // The negative color is obtained by subtracting each channel from 255.
                    int r = 255 - c.getRed();
                    int g = 255 - c.getGreen();
                    int b = 255 - c.getBlue();

                    // overwrite the original pixel at (x, y) with the new color
                    img.setRGB(x, y, new Color(r, g, b).getRGB()); //Create a new Color object using the inverted RGB values and convert it back to a packed integer format
                }
            }
        }

        // Return the same image with the negative applied in-place.
        // The working image reference stays the same in ImageProcessor.
        System.out.println("Negative colors applied.");
        return img;
    }
}