import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawTriangle {
    public static void draw(BufferedImage image, String fileName){
        try {
            ImageIO.write(image, "jpg", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }



}
