import java.awt.image.BufferedImage;
import java.io.IOException;

// Class for operation 1: CROP
// When the user crops, the returned image becomes the new working image.
// Any previous modifications outside the cropped area are discarded.
public class CropOperation implements ImageOperation {

    @Override
    public BufferedImage apply(BufferedImage img, Region region) throws IOException {

        int cropWidth  = region.getCropWidth();
        int cropHeight = region.getCropHeight();

        // using TYPE_INT_ARGB so that the background outside the triangle is transparent
        BufferedImage croppedImg = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = region.getMinY(); y <= region.getMaxY(); y++) {  //scan the image from the bounding box
            for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
                if (region.isPointInside(x, y)) { //call to the method isPointInside
                    // gets the values from the pixel where it is
                    int rgb = img.getRGB(x, y); // give to the variable rgb the pixel from the image img color in format ARGB
                    //Paint on the NEW image (coordinates relative to the crop)
                    croppedImg.setRGB(x - region.getMinX(), y - region.getMinY(), rgb); // x - minX and y - minY to transform the absolute coordinate into relative coordinates
                    // croppedImg have the same size as the bounding box
                }
            }
        }

        // The cropped image is the new working image.
        // The caller (ImageProcessor) will replace its current image with this result.
        System.out.println("Crop applied. Working image is now " + cropWidth + "x" + cropHeight + " pixels.");
        return croppedImg;
    }
}