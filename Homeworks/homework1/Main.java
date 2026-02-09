package homework1;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int opc;
        boolean whil = true;

        System.out.println("what figure?");
        System.out.println("1. square");
        System.out.println("2. rectangle");
        System.out.println("3. triangle");
        System.out.println("4. circle");
        System.out.println("5. regular pentagon");

        while(whil) {
            System.out.println("from what figure you want area and perimeter?");
            opc = input.nextInt();
            input.nextLine();

            switch (opc) {
                case 1:
                    System.out.println("insert a side");
                    double a = input.nextInt();
                    double perimeter = a + a + a + a;
                    double area = a * a;
                    System.out.println("Area of square is: " + area);
                    System.out.println("Perimeter of square is: " + perimeter);

                    break;
                case 2:
                    System.out.println("insert base");
                    double base = input.nextInt();
                    System.out.println("insert height");
                    double height = input.nextInt();

                    double rectarea = base * height;
                    double rectperimeter = height + base + height + base;
                    System.out.println("Area is: " + rectarea);
                    System.out.println("Perimeter is: " + rectperimeter);
                    break;
                case 3:
                    System.out.println("insert base");
                    double tbase = input.nextInt();
                    System.out.println("insert height");
                    double ttype = input.nextInt();

                    double tarea = (tbase * ttype) / 2;
                    double  tperimeter = tbase+tbase+tbase;
                    System.out.println("Area is: " + tarea);
                    System.out.println("Perimeter is: " + tperimeter);
                    break;
                case 4:
                    double pi = 3.14;
                    System.out.println("insert diameter");
                    double diameter = input.nextInt();
                    System.out.println("insert radio");
                    double rad = input.nextInt();

                    double carea= pi*(rad*rad);
                    double cperimeter = pi*diameter;
                    break;
                case 5:
                    System.out.println("insert a side");
                    double pside = input.nextInt();
                    System.out.println("insert apothem");
                    double apothem = input.nextInt();

                    double pperimeter = pside*5;
                    double parea = (pperimeter*apothem)/2;

                    System.out.println("perimeter is: " + pperimeter);
                    System.out.println("area is: " + parea);
                    break;
                case 6:
                    whil = false;
                    input.close();
                    break;
                default:
                    System.out.println("Invalid input");
                    break;

            }
        }

    }
}



