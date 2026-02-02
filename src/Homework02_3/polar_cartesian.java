package Homework02_3;

public class polar_cartesian {
private static final double degrees_to_radians = Math.PI/180; //constant to convert degrees to radians
double x;
double y;
double magnitude;
double angle_degrees;

    public polar_cartesian() {}

    public polar_cartesian(double magnitude, double angle_degrees) {
        this.magnitude = magnitude;
        this.angle_degrees = angle_degrees;
        conversion(angle_degrees);
        components(magnitude, angle_degrees);
        new_coordinate();
    }


    public double conversion(double angle_degrees) {
        this.angle_degrees = angle_degrees;
        double angle_radians = angle_degrees * degrees_to_radians;
        return angle_radians;
    }

    public void components (double magnitude, double angle_degrees) {
        this.magnitude = magnitude;

        y = magnitude * Math.sin(conversion(angle_degrees));
        x = magnitude * Math.cos(conversion(angle_degrees));

    }

    public void new_coordinate (){
        System.out.println("("+x+","+y+")");
    }

}
