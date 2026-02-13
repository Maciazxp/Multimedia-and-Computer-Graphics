import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) {
    try {
        String square = Files.readString(Path.of("src/square.txt"));
        try (FileWriter writer = new FileWriter("square.svg")) {
            writer.write(square);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
        }


        try {
            String landScape = Files.readString(Path.of("src/LandScape.txt"));
            try (FileWriter writer = new FileWriter("landScape.svg")) {
                writer.write(landScape);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}