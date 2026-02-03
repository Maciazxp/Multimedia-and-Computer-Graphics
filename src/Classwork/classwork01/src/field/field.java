package Classwork.classwork01.src.field;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class field {
    public static void main(String[] args) {
        BufferedImage image = new  BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);

        //White background
        for (int y = 0; y < 600; y++) { //loop through all rows (y-axis)
            for (int x = 0; x < 800; x++) { //loop through all columns (x-axis)
                image.setRGB(x, y, Color.white.getRGB()); //set the pixel white
            }
        }

        //Sun data (center and radio)
        int cx = 150; //Center x axis
        int cy = 120; //Center y axis
        int r = 60; //Radio

        //Draw the Sun
        for (int y = cy - r; y <= cy + r; y++) {
            for (int x = cx - r; x <= cx + r; x++) {
                //distance between the current pixel and the center
                int dx = x - cx;
                int dy = y - cy;
                //if the pixel is inside the circle, it turns yellow
                if (dx*dx + dy*dy <= r*r) { //circle equation
                    image.setRGB(x, y, Color.yellow.getRGB());
                }
            }
        }

        //Sunlight Big lines
        for (int i =0; i<4;i++){ //draw 4 large lines
            double angle = i*Math.PI/2; //angle in radians
            for (int j = r; j< r+35; j++){ //starts drawing from the Sun edge
                int x = (int)(cx + j * Math.cos(angle));
                int y = (int)(cy + j * Math.sin(angle));
                image.setRGB(x, y, Color.RED.getRGB()); //make the lines red
            }
        }
        //Sunlight Small lines
        for (int p =0; p<8;p++){
            double angle = p*Math.PI/4; //angle every 45 degrees
            for (int o = r; o< r+20; o++){
                int x = (int)(cx + o * Math.cos(angle));
                int y = (int)(cy + o * Math.sin(angle));
                image.setRGB(x, y, Color.RED.getRGB());
            }
        }

        //Mountain
        //data (it can be changed)
        int verticalP = 430; //vertical position of the mountain (or ground)
        int height = 32; //height of the mountains
        double frequency = 0.05; //frequency or width of the mountains

        //loops through every pixel
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {

                //sin function to create the mountain shape
                //uses the vertical position, the height and width to create the mountain
                double mountain = verticalP + height * Math.sin(x * frequency);

                if (y > mountain) { //if the pixel is below the sin function, it turns green
                    image.setRGB(x, y, Color.GREEN.getRGB());
                }
            }
        }

        //saves the file as jpg
        File outputImage = new File("field.jpg");
        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
