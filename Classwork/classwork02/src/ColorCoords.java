import java.awt.*;
import java.awt.image.BufferedImage;


public class ColorCoords {
    private Triangle triangle;
    private int width;
    private int height;

    public ColorCoords(Triangle triangle, int width, int height) {
        this.triangle = triangle;
        this.width = width;
        this.height = height;
    }

    public BufferedImage bufferedImage () {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double denominator = (triangle.yB - triangle.yC) * (triangle.xA - triangle.xC) + (triangle.xC - triangle.xB) * (triangle.yA - triangle.yC);

        //pass for every pixel and calculate the lambdas and color (if the pixel is inside the triangle)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double D1 = ((triangle.yB - triangle.yC) * (x - triangle.xC) + (triangle.xC - triangle.xB) * (y - triangle.yC)) / denominator;
                double D2 = ((triangle.yC - triangle.yA) * (x - triangle.xC) + (triangle.xA - triangle.xC) * (y - triangle.yC)) / denominator;
                double D3 = 1 - D1 - D2;


                if (D1 >= 0 && D2 >= 0 && D3 >= 0) {
                    int r = (int) (D1 * 255);
                    int g = (int) (D2 * 255);
                    int b = (int) (D3 * 255);
                    image.setRGB(x, y, new Color(r, g, b).getRGB());
                }else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }

        }
        return image;
    }
}