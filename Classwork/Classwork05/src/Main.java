import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // enter the text file path
            System.out.println("enter the file path of the .txt:");
            String filePath = scanner.nextLine();

            // reads the text content and saves it
            String text = new String(Files.readAllBytes(Paths.get(filePath)));

            // ask for the language to translate
            System.out.println("¿language to translate?");
            String language = scanner.nextLine();

            // create the prompt to send to the API
            text = text.replace("\n", " ").replace("\r", " ");
            String prompt = "Translate the following text into the language " + language + ": " + text;


            // reads the environment variable (The environment variable contains the API key)
            String token = System.getenv("OpenAIToken");

            String chatPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"");

            String json = "{" + "\"model\":\"gpt-4.1-mini\"," + "\"input\":\"" + chatPrompt + "\"" + "}";


            File jsonFile = new File("request.json");
            FileWriter jsonWriter = new FileWriter(jsonFile);
            jsonWriter.write(json);
            jsonWriter.close();

            String[] command = {
                    "curl",
                    "-s",
                    "--max-time", "20",
                    "https://api.openai.com/v1/responses",
                    "-H", "Content-Type: application/json",
                    "-H", "Authorization: Bearer " + token,
                    "-d", "@request.json"
            };

            System.out.println(json);

            //create the process builder with the command to execute
            ProcessBuilder builder = new ProcessBuilder(command);
            //start to execute the command
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            // wait for the process to finish
            process.waitFor();

            // saves the result in another file
            String outputFile = "translated_file.txt";

            FileWriter writer = new FileWriter(outputFile);
            writer.write(response.toString());
            writer.close();

            System.out.println("translated file saved in: " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}