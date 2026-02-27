import java.awt.image.BufferedImage;
import java.io.IOException;

// interface for all image operations that can be applied to a region.
public interface ImageOperation {

    // Applies the operation to the given image within the specified region.
    // Returns the resulting image after the operation
    //The param img is the source image to work on
    // The param region is the region that delimits which pixels to process
    // throws IOException if an I/O error occurs during processing
    BufferedImage apply(BufferedImage img, Region region) throws IOException;
}