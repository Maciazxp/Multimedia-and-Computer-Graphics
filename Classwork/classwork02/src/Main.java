

import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        Triangle triangle = new Triangle(100,350,400,350,250,100); //set the coords of the vertex from the triangle


        ColorCoords image = new ColorCoords(triangle,500,500);
        BufferedImage BufferedImage = image.bufferedImage();

        DrawTriangle.draw(BufferedImage,"triangle2.jpg");
    }
}



//I first made it in a god class in one function, then I changed to poo.
  /*
        BufferedImage image = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);

        int xA = 100, xB = 400, xC = 250;
        int yA = 350, yB = 350, yC = 100;

        for (int y = 0; y < 500; y++) {
            for (int x = 0; x < 500; x++) {
                double D1 = (double) ((yB - yC) * (x - xC) + (xC - xB) * (y - yC)) /((yB-yC)*(xA-xC)+(xC-xB)*(yA-yC));
                double D2 = (double) ((yC - yA) * (x - xC) + (xA - xC) * (y - yC)) /((yB-yC)*(xA-xC)+(xC-xB)*(yA-yC));
                double D3 = 1-D1-D2;
                double p = D1+D2+D3;
                if (D1 >= 0 && D2 >= 0 && D3 >= 0){
                    int r = (int) (D1 *255);
                    int g= (int)(D2*255);
                    int b= (int)(D3*255);
                    image.setRGB(x, y, new Color(r, g, b).getRGB());
                }else {
                    image.setRGB(x, y, new Color(0, 0, 0).getRGB());
                }
            }
        }
        //saves the file as jpg
        File outputImage = new File("triangle.jpg");
        try {
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */