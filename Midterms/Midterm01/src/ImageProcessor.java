import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Responsible for loading the source image, applying operations over time, and saving the final result once all modifications are done.
public class ImageProcessor {

    // The current working image. This may be replaced if the user performs a crop, in which case the cropped image becomes the new working image.
    private BufferedImage currentImg;

    // param filePath path to the source image
    // throws IOException if the file cannot be read
    // throws RuntimeException if the file does not exist
    public ImageProcessor(String filePath) throws IOException {
        // load the image and save it in inputFile
        File inputFile = new File(filePath);
        if (!inputFile.exists()) { //error if there is no image found
            System.out.println("Error there is no image.");
            throw new RuntimeException("Image not found: " + filePath);
        }
        this.currentImg = ImageIO.read(inputFile); //create the bufferedImage with the image we save in inputFile
    }

    // Returns the current working image.
    // After a crop, this will be the cropped image, otherwise it is the original.
    public BufferedImage getImage() {
        return currentImg;
    }

    // Applies the given operation to the current working image within the specified region.
    // If the operation is a CropOperation, the returned image becomes the new working image.
    // For Negative and Rotate, the same image is modified in place and kept as the working image.
    // The param operation is for the operation to execute
    // the param region is for the region that delimits which pixels to process
    // throws IOException if an I/O error occurs during processing
    public void process(ImageOperation operation, Region region) throws IOException {
        // The operation returns the resulting image:
        //   - CropOperation: a brand-new smaller BufferedImage (becomes the new working image)
        //   - NegativeOperation / RotateOperation: the same currentImg modified in that place
        currentImg = operation.apply(currentImg, region);
    }

    // Saves the current working image to disk as the final output.
    // Called once after all modifications have been applied.
    // The param outputPath refers to the file path where the result will be written
    // throws IOException if writing the file fails
    public void saveResult(String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        if (ImageIO.write(currentImg, "png", outputFile)) {
            System.out.println("image saved as " + outputFile.getAbsolutePath());
        } else {
            System.out.println("Error saving the image.");
        }
    }
}
