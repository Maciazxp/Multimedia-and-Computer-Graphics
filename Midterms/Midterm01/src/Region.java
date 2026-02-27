import java.awt.image.BufferedImage;

/*
Represents a rectangular region defined by four corner vertices (A, B, C, D)
on a BufferedImage. Computes the bounding box and barycentric denominators
*/

public class Region {
    // Corner vertices of the quadrilateral
    private final int xA, yA; // bottom-left
    private final int xB, yB; // bottom-right
    private final int xC, yC; // top-right
    private final int xD, yD; // top-left

    // Bounding box
    private final int minX, maxX, minY, maxY;

    // Denominators used in barycentric coordinate calculations
    private final double denom1, denom2;

    /*
     Builds the region from the top-left corner plus width and height,
     and set the bounding box to the image dimensions.

     The param img is the image this region refers to
     xLeft: top-left X coordinate
     yTop: top-left Y coordinate
     widthRect: region width
     heightRect: region height
     */
    public Region(BufferedImage img, int xLeft, int yTop, int widthRect, int heightRect) {
        // Calculate coordinates
        xD = xLeft;
        yD = yTop;
        xC = xLeft + widthRect;
        yC = yTop;
        xA = xLeft;
        yA = yTop + heightRect;
        xB = xLeft + widthRect;
        yB = yTop + heightRect;

        int width  = img.getWidth();
        int height = img.getHeight();

        // Bounding box for the square formed
        minX = Math.max(0, Math.min(Math.min(xA, xB), Math.min(xC, xD))); //0 to avoid negative coords
        //First get the smallest X between A and B, then the smallest X between C and D
        //Then gets the smallest X of the four vertices. To get the leftmost point of the quadrilateral.
        //Apply the same logic to get the other points
        maxX = Math.min(width  - 1, Math.max(Math.max(xA, xB), Math.max(xC, xD))); //gets the rightmost point (-1 If the user places a point outside the image)
        minY = Math.max(0, Math.min(Math.min(yA, yB), Math.min(yC, yD)));
        maxY = Math.min(height - 1, Math.max(Math.max(yA, yB), Math.max(yC, yD)));

        // define the denominators that the formula would have
        denom1 = (yB - yC) * (xA - xC) + (xC - xB) * (yA - yC);
        denom2 = (yC - yD) * (xA - xD) + (xD - xC) * (yA - yD);
    }


    //Method to determine if a point (x,y) is inside the quadrilateral formed
    //by A,B,C,D using barycentric coordinates (division into two triangles).

    public boolean isPointInside(int x, int y) {
        // -------- TRIANGLE 1 (A,B,C) --------
        double D1 = ((yB - yC) * (x - xC) + (xC - xB) * (y - yC)) / denom1;
        double D2 = ((yC - yA) * (x - xC) + (xA - xC) * (y - yC)) / denom1;
        double D3 = 1 - D1 - D2;
        if (D1 >= 0 && D2 >= 0 && D3 >= 0) return true;

        // -------- TRIANGLE 2 (A,C,D) --------
        double E1 = ((yC - yD) * (x - xD) + (xD - xC) * (y - yD)) / denom2;
        double E2 = ((yD - yA) * (x - xD) + (xA - xD) * (y - yD)) / denom2;
        double E3 = 1 - E1 - E2;
        return (E1 >= 0 && E2 >= 0 && E3 >= 0);
    } //when isPointInside is true, it confirms that the pixel is inside the two triangles


    //Getters
    public int getMinX()  {
        return minX;
    }
    public int getMaxX()  {
        return maxX;
    }
    public int getMinY()  {
        return minY;
    }
    public int getMaxY()  {
        return maxY;
    }


    //Width of the bounding box (+1 for inclusive range)
    public int getCropWidth()  {
        return maxX - minX + 1;
    }
    //Height of the bounding box (+1 for inclusive range)
    public int getCropHeight() {
        return maxY - minY + 1;
    }
}