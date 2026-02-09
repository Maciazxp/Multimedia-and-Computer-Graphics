package Classwork.classwork01.src.clock;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class clock {
    public static void main(String[] args) {
        BufferedImage image = new  BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);

        //Black background
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {
                image.setRGB(x, y, Color.BLACK.getRGB());
            }
        }

        //Circle data (center and radio)
        int cx = 800 /2; //Centre x axis
        int cy = 600 /2; //Centre y axis
        int r = 200; //Radio

        //Draw the circle
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {
                int dx = x - cx;
                int dy = y - cy;
                double dist = Math.sqrt(dx*dx + dy*dy); //distance to the center


                if (Math.abs(dist - r) < 0.5) { //if it's close to the radius, turn the pixel white
                    image.setRGB(x, y, Color.white.getRGB());
                }
            }
        }

        //Draw the dots for the hours
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12; //angle for each hour

            //Calculate the position of the point (inside the circle)
            int px = (int)(cx + (r-40) * Math.cos(angle));
            int py = (int)(cy + (r-30) * Math.sin(angle));

            //For to set the size of the dots, starts in -1 and go to 0, so there are 2x2
            for (int dy = -1; dy <= 0; dy++) {
                for (int dx = -1; dx <= 0; dx++) {
                    image.setRGB(px + dx, py + dy, Color.white.getRGB());
                }
            }
        }

        //Calculate the angle of the lines
        int hour = 10; //To set the hour (it can be changed)
        int minute = 1; //To set the minute (it can be changed)
        double angleMin = -Math.PI / 2 + 2 * Math.PI * (minute / 12.0);
        double angleHour = -Math.PI / 2 + 2 * Math.PI * (hour / 12.0);

        //calculate the end of the lines
        int minX = (int)(cx + r * 0.75 * Math.cos(angleMin)); //mins are larger, that's why r*0.75
        int minY = (int)(cy + r * 0.75 * Math.sin(angleMin));

        int hourX = (int)(cx + r * 0.4 * Math.cos(angleHour)); //hours are shorter
        int hourY = (int)(cy + r * 0.4 * Math.sin(angleHour));

        //Draw the lines by calling the drawLine method
        drawLine(image, cx, cy, minX, minY); //line for minutes
        drawLine(image, cx, cy, hourX, hourY); //line for hours



        //saves the file as jpg
        File outputImage = new File("clock.jpg");
        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    //Method to draw minute and hour line using linear interpolation
    static void drawLine(BufferedImage img, int x1, int y1, int x2, int y2) {
        int steps = 1000;
        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps; //interpolation factor
            int x = (int)(x1 + t * (x2 - x1)); //Point x in the line
            int y = (int)(y1 + t * (y2 - y1)); //point y in the line
            img.setRGB(x, y, Color.white.getRGB()); //draw the pixel
        }
    }
}
