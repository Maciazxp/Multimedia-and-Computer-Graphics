package Classwork.classwork01.src.square;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Square {
    public static void main(String[] args) {
        BufferedImage image = new  BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
        //image setRGB(200,200, Color.yellow.getRGB());

        int aux=1;

        for (int x = 0; x < 400; x++){
            for (int y = 0; y < 400; y++){
                image.setRGB(x, y, Color.BLUE.getRGB());
            }
        }

        for (int x = 0; x<400; x++){
            for (int y = 0; y<aux; y++){
                //image.setRGB(x,y,image.getRGB(x,y));
                image.setRGB(x,y, Color.RED.getRGB());
            }
            aux++;
        }



        File outputImage = new File("square.jpg");
        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}