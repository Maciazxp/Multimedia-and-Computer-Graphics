package Homework02_3;

import java.util.InputMismatchException;
import java.util.Scanner;

public class menu {
    private final Scanner scanner;


    public menu() { //menu constructor where everything is called
        scanner = new Scanner(System.in);
        showmenu(); //call to the menu method
        opcionmenu(); //call option menu to start an action
    }

    public void showmenu() { //menu method that shows the options
        System.out.println("Menu");
        System.out.println("1. polar to cartesian");
        System.out.println("2. cartesian to polar");
    }

    public void opcionmenu() { //method to manage the input of the menu
        int opcion;
        do { // do while to close when the third option is chosen
            System.out.println("select an option");
            opcion = validoption(); //the option is validated by the validoption method
            switch (opcion) { //switch case for the different options
                case 1:
                    p_c(); //call to the polar to cartesian method
                    break;
                case 2:
                    c_p(); //call to de cartesian to polar method
                    break;
                case 3:

                    break;

                default:
                    System.out.println("invalid option");
            }
            if (opcion != 3) {
                scanner.nextLine();
            }
        } while (opcion != 3); // The process is not finished while choosing 1 or 2, only 3
        scanner.close();
    }

    public int validoption() { //method to accept a valid option in optionmenu
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; //if is an invalid option, it returns -1 to ask for another one
        }
    }

    public void p_c() { //polar to cartesian method
        try { //try catch to avoid symbols or letters
            System.out.println("enter magnitude (radio)");
            double magnitude = scanner.nextDouble();
            System.out.println("enter angle (in degrees)");
            double angle_degrees = scanner.nextDouble();
            polar_cartesian pc = new polar_cartesian(magnitude, angle_degrees);
        }catch (InputMismatchException e) {
            System.out.println("Please enter valid numbers (not letters or symbols)");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine();
        }
    }

    public void c_p() { //cartesian to polar method
        double xcord=validate_x(); //call to the method that validates the input for x and assign it to a variable
        double ycord=validate_y(); // call to the method that validates the input for y and assign it to a variable
        cartesian_polar cp = new cartesian_polar(xcord, ycord);

    }

    public double validate_x() { //method that only accepts valid numbers to x (no symbols or letters)
        double validInputx = 0;
        boolean valid = false;
        while (!valid) {
            try { //try catch to avoid invalid inputs
                System.out.println("enter the component x");
                validInputx = scanner.nextDouble();
                valid = true;
            } catch (InputMismatchException e) {
                System.out.println("Please enter valid numbers (not letters or symbols)");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        }
        return validInputx; //return the value once it was accepted
    }


    public double validate_y() { //method that only accepts valid numbers to y (no symbols or letters)
        double validInputy = 0;
        boolean valid = false;
        while (!valid) {
            try { //try catch to avoid invalid inputs
                System.out.println("enter the component y");
                validInputy = scanner.nextDouble();
                valid = true;
            } catch (InputMismatchException e) {
                System.out.println("Please enter valid numbers (not letters or symbols)");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine();
            }
        }
        return validInputy; //return the value once it was accepted
    }


}
