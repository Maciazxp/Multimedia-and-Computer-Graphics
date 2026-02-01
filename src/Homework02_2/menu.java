package Homework02_2;

import java.util.InputMismatchException;
import java.util.Scanner;


public class menu {
        private final Scanner scanner;


        public menu() { //menu constructor to initialize everything
                this.scanner = new Scanner(System.in);
                startmenu(); //call to the method startmenu
        }

        public void startmenu() { //method start menu
                boolean validInput = false;

                while (!validInput) {
                        try { //try catch to avoid invalid inputs (0 or negative numbers)
                                System.out.println("Enter width and height:");

                                System.out.print("Width: ");
                                double x = scanner.nextDouble();

                                System.out.print("Height: ");
                                double y = scanner.nextDouble();

                                // accept only positive numbers
                                if (x <= 0 || y <= 0) {
                                        System.out.println("Both values must be greater than 0");
                                        continue;
                                }

                                calculator calc = new calculator(x, y); //if both numbers are valid, the calculator starts
                                validInput = true;

                        } catch (InputMismatchException e) {
                                System.out.println("Please enter valid numbers (not letters or symbols)");
                                scanner.nextLine();
                        } catch (Exception e) {
                                System.out.println("An error occurred: " + e.getMessage());
                                scanner.nextLine();
                        }

                }
        }
}