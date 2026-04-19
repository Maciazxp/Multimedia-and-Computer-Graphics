import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // API key is expected as an environment variable
        String openAIKey = System.getenv("OpenAIToken");

        if (openAIKey == null) {
            System.out.println("you dont have the OpenAIToken, please create it."); // same variable, if not it throws error
            sc.close();
            return;
        }

        // Collect media file paths from the user
        System.out.print("How many files do you want to process? please videos no longer than 5 sec ");
        System.out.println("(make sure that all of the files have metadata)");
        int NumberFiles = sc.nextInt();
        sc.nextLine();

        MediaFile[] media = new MediaFile[NumberFiles];

        for (int i = 0; i < NumberFiles; i++) {
            System.out.print("Path for file " + (i + 1) + ": ");
            String path = sc.nextLine().trim();
            media[i] = new MediaFile(path); // metadata is extracted automatically on construction
        }

        //Sort media from oldest to newest
        SortMedia.sortByDate(media);
        System.out.println("\nFiles sorted by date (oldest to newest).");

        // Identify the oldest and newest file depending on date
        MediaFile oldest = SortMedia.getOldest(media);
        MediaFile newest = SortMedia.getNewest(media);

        OpenAICall api = new OpenAICall();

        // Step 1: Analyze visual content
        System.out.println("\n[Step 1/7] starting to analyze the files");
        String visualDescription = api.analyzeVisualContent(media, openAIKey);

        //Step 2: Generate opening essence image using the analysis
        System.out.println("\n[Step 2/7] creating AI image");
        String essencePrompt = api.AiImagePrompt(media, visualDescription);
        api.generateImage(essencePrompt, openAIKey, "AiImage.png");

        //Step 3: Generate map phrase (also to use in the narration closing line)
        System.out.println("\n[Step 3/7] Generating map closing phrase");
        String phrase = api.generateText(api.MapPhrasePrompt(oldest, newest), openAIKey);

        //Step 4: Generate narration script using the actual images + map phrase as closing line
        System.out.println("\n[Step 4/7] creating narration script");
        String DescriptionImages = api.generateNarrationScript(media, openAIKey, phrase);
        System.out.println("\n  Generated script:\n" + DescriptionImages);

        // Convert narration text to audio with AI TTS
        System.out.println("\n[Step 5/7] Converting script to audio");
        api.TextToSpeech(DescriptionImages, openAIKey, "audio.mp3");

        //build the map image using the already-generated phrase
        System.out.println("\n[Step 6/7] Building closing map image");
        new MapBuilder().buildMap(oldest, newest, phrase, "mapImage.png");

        //create final video
        System.out.println("\n[Step 7/7] Creating video...");
        String videoname = "final_video_portrait";
        VideoCreator videoCreator = new VideoCreator("audio.mp3", videoname);

        try {
            // VideoCreator implements MediaOperation, so we call execute() directly
            videoCreator.execute(media);
        } catch (Exception e) {
            System.out.println("Video creation failed: " + e.getMessage());
            sc.close();
            return;
        }

        //audio level verification
        System.out.print("\nVerify audio loudness levels? (y/n): ");
        String verify = sc.nextLine().trim().toLowerCase();
        //.trim() removes accidental leading/trailing spaces
        //.toLowerCase() accepts both "Y" and "y" without extra checks

        sc.close();

        if (verify.equals("y") || verify.equals("yes")) {
            videoCreator.verifyAudioLevels(videoname + ".mp4");
        }

        System.out.println("\n DONE THE VIDEO " + videoname + ".mp4");
    }
}
