package Homework02_2;

public class calculator {
    private double x;
    private double y;


    public calculator() {
        this.x = 0;
        this.y = 0;
    }

    public calculator(double x, double y) { //Calculator construct to initialize with x and y
            this.x = x;
            this.y = y;
            System.out.println("the aspect ratio is");
            aspectRatio(x, y); //Call the method aspectratio with the parameters x and y

    }

    //GCD
    private int gcd (int a, int b) { //method to calculate the greatest common divisor


        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private void aspectRatio(double x, double y) { //Aspectratio method to calculate the aspect ratio
        this.x = x;
        this.y = y;

        int xint = (int) Math.round(x); //transform the double into int
        int yint  = (int) Math.round(y);

        int div = gcd(xint, yint); // call the gcd method to calculate the greatest common divisor between x and y

        int aspectx =xint/ div ; //the aspect ratio of x
        int aspecty =yint / div; //the aspect ratio of y

        System.out.println(aspectx + ":" + aspecty);
    }
}
