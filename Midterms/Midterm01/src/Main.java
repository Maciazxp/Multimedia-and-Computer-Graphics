import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            //the image must be on the src project folder.
            //the name and extension also can be changed on this code part if you want to enter another image or format
            ImageProcessor processor = new ImageProcessor("src/up.jpg"); //can be change the name and extension (the one inside the "name.extension")

            // lastRegion keeps the most recently used region so the user can reuse it
            Region lastRegion = null;

            // Keep asking for modifications until the user decides to stop
            boolean keepEditing = true;
            while (keepEditing) {

                // Ask the user whether to use the previous region or define a new one.
                // On the very first iteration there is no previous region, so always ask for coordinates.
                Region region;
                if (lastRegion != null) {
                    System.out.println("\nDo you want to use the same region as before?");
                    System.out.print("Enter 'y' for yes or 'n' to set new coordinates: ");
                    String reuseAnswer = scanner.next().trim().toLowerCase();

                    if (reuseAnswer.equals("y")) {
                        // reuse the region from the previous operation
                        region = lastRegion;
                    } else {
                        // ask the user for new coordinates
                        region = askForRegion(scanner, processor);
                    }
                } else {
                    // First iteration, also means that no previous region exists and ask for coordinates
                    System.out.println("  Top Left (x,y) ───────────── Width\n" +
                            "            │                     │\n" +
                            "            │                     │\n" +
                            "            │                     │\n" +
                            "            │                     │\n" +
                            "          Height ─────────────── ");
                    region = askForRegion(scanner, processor);
                }

                // save the region so it can be reused in the next iteration
                lastRegion = region;

                //show a menu
                System.out.println("\nWhat do you want to do with the image?");
                System.out.println("1. Crop  (the cropped area becomes the new working image)");
                System.out.println("2. Negative Colors");
                System.out.println("3. Rotate - Rotate 90°, 180°, or 270°");
                System.out.print("Select an option (1-3): ");

                int option = scanner.nextInt(); //option for the menu

                // do the selected option
                switch (option) {
                    case 1:
                        processor.process(new CropOperation(), region);
                        // After a crop, the image dimensions change, so the previous region is no longer valid for the new (smaller) working image.
                        lastRegion = null;
                        break;
                    case 2:
                        processor.process(new NegativeOperation(), region);
                        break;
                    case 3:
                        int rotation = 0;
                        boolean validRotation = false;

                        while (!validRotation) {
                            System.out.println("Select rotation: 90, 180, 270");

                            if (scanner.hasNextInt()) {
                                rotation = scanner.nextInt();

                                if (rotation == 90 || rotation == 180 || rotation == 270) {
                                    validRotation = true;
                                } else {
                                    System.out.println("Invalid rotation. Please enter 90, 180, or 270 only.");
                                }
                            } else {
                                System.out.println("Invalid input. Please enter a number (90, 180, or 270).");
                                scanner.next();
                            }
                        }

                        processor.process(new RotateOperation(rotation), region);
                        break;
                    default:
                        System.out.println("Invalid option. No changes applied for this step.");
                }

                // Ask the user if they want to apply another modification
                System.out.print("\nDo you want to apply another modification? (y/n) (other input finish the process): ");
                String continueAnswer = scanner.next().trim().toLowerCase();
                //.trim() deletes blank spaces at the beginning and end of the text, to avoid errors
                //.toLowerCase convert all text to lowercase, to avoid errors and to be more flexible
                keepEditing = continueAnswer.equals("y");
            }

            // when all modifications are done, it saves the final image to disk
            processor.saveResult("edited_image.png");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // Helper method that asks the user to enter the region coordinates and builds a Region from the current working image dimensions.
    // param scanner, the Scanner used to read user input
    // param processor the ImageProcessor holding the current working image
    // returns a new Region built from the user's input
    private static Region askForRegion(Scanner scanner, ImageProcessor processor) {
        System.out.println("Enter the data of where you want the rectangle:");
        System.out.print("Enter top-left X: ");
        int xLeft = scanner.nextInt();
        System.out.print("Enter top-left Y: ");
        int yTop = scanner.nextInt();
        System.out.print("Enter width: ");
        int widthRect = scanner.nextInt();
        System.out.print("Enter height: ");
        int heightRect = scanner.nextInt();

        // Build the region using the current working image (may be the cropped image)
        return new Region(processor.getImage(), xLeft, yTop, widthRect, heightRect);
    }
}