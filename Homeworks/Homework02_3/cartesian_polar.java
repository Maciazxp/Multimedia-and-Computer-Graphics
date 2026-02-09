package Homework02_3;

public class cartesian_polar {
    double compx;
    double compy;

    cartesian_polar(double x, double y) {
        compx = x;
        compy = y;
        new_coordinate(compx, compy);

    }

    public double radio (double x, double y) {
        double radio = Math.sqrt(x*x + y*y);
        return radio;
    }


    public double theta (double x, double y) {
        double thetaradians = Math.atan2(y, x);
        if (thetaradians < 0) {
            thetaradians += 2 * Math.PI;
        }

        double theta = thetaradians * 180 / Math.PI;

        return theta;
    }

    public void new_coordinate (double x, double y) {
        double r = radio(x, y);
        double angle = theta(x, y);
        System.out.println("("+r+","+angle+")");
    }


}
