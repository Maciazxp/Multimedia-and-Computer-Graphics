import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // read image (the image must be in the src rute)
            BufferedImage img = ImageIO.read(new File("src/i.jpg"));
            int width = img.getWidth();
            int height = img.getHeight();

            // set to a 1D array
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = img.getRGB(x, y);
                }
            }

            //method to compress
            Compressor compressor = new Compressor();
            compressor.compress(pixels, width, height, "compressedImage.mic");

            //method to decompress
            Descompressor decompressor = new Descompressor();
            decompressor.decompress("compressedImage.mic", "newImage.jpg");

        }catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}