import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Class for operation 3: ROTATE
// Rotates the content inside the region and pastes it back into the working image.
public class RotateOperation implements ImageOperation {

    private final int degrees; // only accept 90, 180, and 270 degrees

    // degree rotation angle must be 90, 180, or 270
    public RotateOperation(int degrees) {
        this.degrees = degrees;
    }

    @Override
    public BufferedImage apply(BufferedImage img, Region region) throws IOException {

        // create a new image for the crop
        int cropWidth  = region.getCropWidth();  //it gets the width that the crop will have (+1 to have all the element range because is an inclusive range)
        int cropHeight = region.getCropHeight(); //it gets the height that the crop will have (+1 to have all the element range, also is an inclusive range)

        // Extract the contents of the square
        BufferedImage sourcePatch = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);
        for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                if (region.isPointInside(x, y)) {
                    sourcePatch.setRGB(x - region.getMinX(), y - region.getMinY(), img.getRGB(x, y));
                }
            }
        }

        //Create an image for the rotated patch
        // If it's 90 or 270, the width and height are swapped
        BufferedImage rotatedPatch;
        if (degrees == 90 || degrees == 270) {
            rotatedPatch = new BufferedImage(cropHeight, cropWidth, BufferedImage.TYPE_INT_ARGB);
        } else {
            rotatedPatch = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);
        }

        // Rotate depending on the user selection
        for (int y = 0; y < cropHeight; y++) {
            for (int x = 0; x < cropWidth; x++) {
                int rgb = sourcePatch.getRGB(x, y);
                if ((rgb >> 24) != 0x00) { //If the pixel is not transparent
                    if (degrees == 90) {
                        rotatedPatch.setRGB(cropHeight - 1 - y, x, rgb);
                    } else if (degrees == 180) {
                        rotatedPatch.setRGB(cropWidth - 1 - x, cropHeight - 1 - y, rgb);
                    } else if (degrees == 270) {
                        rotatedPatch.setRGB(y, cropWidth - 1 - x, rgb);
                    }
                }
            }
        }

        // clean the original area, painting it white
        for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                if (region.isPointInside(x, y)) {
                    img.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        // Calculate centered position to paste the rotated image
        //calculate the center of the original area so that the rotation rotates about its axis.
        int centerX = region.getMinX() + cropWidth / 2;
        int centerY = region.getMinY() + cropHeight / 2;
        //calculate where the rotated patch (which may have inverted dimensions) should begin to be painted.
        int newMinX = centerX - (rotatedPatch.getWidth()  / 2);
        int newMinY = centerY - (rotatedPatch.getHeight() / 2);

        // paste the rotated image
        for (int y = 0; y < rotatedPatch.getHeight(); y++) {
            for (int x = 0; x < rotatedPatch.getWidth(); x++) {
                int rgb = rotatedPatch.getRGB(x, y);

                //only paint if the pixel is not transparent (inside the rotated triangles)
                if ((rgb >> 24) != 0x00) {
                    int targetX = newMinX + x;
                    int targetY = newMinY + y;

                    // validation to doesn't paint outside the complete image
                    if (targetX >= 0 && targetX < img.getWidth() && targetY >= 0 && targetY < img.getHeight()) {
                        img.setRGB(targetX, targetY, rgb);
                    }
                }
            }
        }

        // Return the same image with the rotation applied in-place.
        // The working image reference stays the same in ImageProcessor.
        System.out.println("Rotation of " + degrees + "° applied");
        return img;
    }
}