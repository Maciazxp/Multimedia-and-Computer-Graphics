import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

// Handles all communication with the OpenAI API.
// Responsible for three AI-driven tasks:
//   1. Generating the opening essence image from the media collection
//   2. Generating the full narration script for the video
//   3. Converting the narration script to audio (TTS-1)
// The map inspirational phrase is also generated here via GPT-4o.
public class OpenAICall {

    // OpenAI API base URL
    private static final String OPENAI_BASE_URL = "https://api.openai.com/v1";

    private final HttpClient httpClient;

    public OpenAICall() {
        this.httpClient = HttpClient.newHttpClient();
    }


    // VISUAL ANALYSIS OF THE MEDIA

     //Analyzes the actual visual content of the media files using GPT-4o Vision.
     //It sends the actual images to GPT-4o to get a description of what's
     //visually present, which is then used to generate a more accurate essence image.

     //param media: the sorted array of media files
     //param apiKey: the OpenAI API key
     //return a concise description of the visual content across all media

    public String analyzeVisualContent(MediaFile[] media, String apiKey) {
        try {
            String url = OPENAI_BASE_URL + "/chat/completions";

            // Build multimodal content with text + images
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("[");
            contentBuilder.append("{\"type\":\"text\",\"text\":\"");
            contentBuilder.append(escapeJson(
                    "Look at these images and describe what you see in 2-3 sentences. " +
                            "Focus on: main subjects (people, objects, landscapes), colors, mood, " +
                            "setting (indoor/outdoor, urban/nature), and overall atmosphere. " +
                            "Give a cohesive summary of the visual essence - don't list individual images. " +
                            "This description will be used to generate an artistic cover image."
            ));
            contentBuilder.append("\"}");

            // Add each media file — extract a frame first if it's a video
            int imagesAdded = 0;
            for (MediaFile m : media) {
                File imgFile;
                boolean isTemp = false;

                if (SortMedia.forVideo(m.mediaPath)) {
                    imgFile = extractVideoFrame(new File(m.mediaPath));
                    isTemp = true;
                } else {
                    imgFile = new File(m.mediaPath);
                }

                if (imgFile != null && imgFile.exists() && imgFile.length() < 20 * 1024 * 1024) {
                    String base64 = encodeImageToBase64(imgFile);
                    contentBuilder.append(",{\"type\":\"image_url\",\"image_url\":{\"url\":\"");
                    contentBuilder.append("data:image/jpeg;base64,");
                    contentBuilder.append(base64);
                    contentBuilder.append("\",\"detail\":\"low\"}}");
                    imagesAdded++;
                }

                if (isTemp && imgFile != null) imgFile.delete();
                if (imagesAdded >= 5) break; // Limit to 5 images to avoid token limits
            }
            contentBuilder.append("]");

            String requestBody = "{"
                    + "\"model\": \"gpt-4o\","
                    + "\"messages\": [{"
                    + "\"role\": \"user\","
                    + "\"content\": " + contentBuilder.toString()
                    + "}],"
                    + "\"max_tokens\": 150"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String description = extractTextFromResponse(response.body());
                System.out.println("    Visual analysis: " + description);
                return description;
            } else {
                System.out.println("    Vision analysis failed. Status: " + response.statusCode());
                return buildFallbackDescription(media);
            }

        } catch (Exception e) {
            System.out.println("    Error analyzing media: " + e.getMessage());
            return buildFallbackDescription(media);
        }
    }


     //Encodes an image file to base64 for sending to GPT-4o Vision.

    private String encodeImageToBase64(File imageFile) throws IOException {
        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Extracts a single frame from a video file at the first second (or it can be changed) using FFmpeg.
    // Returns a temp File with the frame as JPEG, or null if extraction fails.
    // param videoFile: the source video file
    private File extractVideoFrame(File videoFile) {
        try {
            File frameFile = new File("frame_tmp_" + videoFile.getName() + ".jpg");
            String[] command = {
                "ffmpeg", "-y",
                "-i", videoFile.getAbsolutePath(),
                "-ss", "00:00:01",
                "-vframes", "1",
                frameFile.getAbsolutePath()
            };
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
            return frameFile.exists() ? frameFile : null;
        } catch (Exception e) {
            System.out.println("    failed the extraction of the frame: " + videoFile.getName());
            return null;
        }
    }

    //Fallback description if vision analysis fails.
    private String buildFallbackDescription(MediaFile[] media) {
        return "a collection of " + media.length + " personal travel moments";
    }


    // IMAGE GENERATION (DALL-E 3)


     //Builds the prompt for the opening AiImage using visual analysis results.
     //param media: the sorted array of media files
     //param visualDescription: the description from GPT-4o Vision analysis
    // return a prompt string ready to send to DALL-E 3

    public String AiImagePrompt(MediaFile[] media, String visualDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a single artistic image that captures this visual essence: ");
        sb.append(visualDescription).append(" ");

        // Add location context if available (as supplementary info)
        boolean hasGPS = false;
        StringBuilder locations = new StringBuilder();
        for (MediaFile m : media) {
            if (!m.GPS.equals("No data")) {
                locations.append(m.GPS).append("; ");
                hasGPS = true;
            }
        }

        if (hasGPS) {
            sb.append("The scene is set in/around: ").append(locations).append(" ");
        }

        sb.append("Style: Cinematic, artistic, like a travel documentary cover. ");
        sb.append("Make it cohesive, emotional, and visually striking.");

        return sb.toString();
    }

    // Builds the prompt for the short inspirational phrase overlaid on the closing map.
    // Uses the GPS coordinates of the first and last media files.
    // param oldest: the first media file in the sorted array
    // param newest: the last media file in the sorted array
    // returns a prompt asking GPT-4o for a short poetic phrase about the journey
    public String MapPhrasePrompt(MediaFile oldest, MediaFile newest) {
        return "Generate a short, poetic, and inspirational phrase (1 sentences max) " +
               "for someone who traveled from GPS coordinates " + oldest.GPS +
               " to " + newest.GPS + ". " +
               "The phrase should feel personal and evocative of the places visited. " +
               "Return only the phrase, no extra text.";
    }

    // Sends the prompt to DALL-E 3 and saves the resulting image to disk.
    // Requests a 1024x1792 image in b64_json format to avoid redirect issues.
    // param prompt: the image generation prompt
    // param apiKey: the OpenAI API key
    // param outputPath: where to save the resulting PNG file
    public void generateImage(String prompt, String apiKey, String outputPath) {
        try {
            String url = OPENAI_BASE_URL + "/images/generations";

            String requestBody = "{"
                    + "\"model\": \"dall-e-3\","
                    + "\"prompt\": \"" + escapeJson(prompt) + "\","
                    + "\"n\": 1,"
                    + "\"size\": \"1024x1024\","          // 1024x1024
                    + "\"response_format\": \"b64_json\""  // base64 so we can save directly without a redirect
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey) // OpenAI uses Bearer token auth
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String base64Image = extractBase64Image(response.body());
                byte[] imageBytes  = Base64.getDecoder().decode(base64Image);
                Files.write(Paths.get(outputPath), imageBytes);
                System.out.println("    Image saved: " + outputPath);
            } else {
                System.out.println("    Image generation failed. Status: " + response.statusCode());
                System.out.println("    Response: " + response.body());
            }

        } catch (Exception e) {
            System.out.println("    Error generating image: " + e.getMessage());
        }
    }

    // NARRATION SCRIPT GENERATION with GPT-4o

    // Sends the actual media images to GPT-4o Vision and asks it to describe
    // what is literally visible in each one, in chronological order.
    // The result is a plain narration script ready for TTS — no invented stories.
    // param media: the sorted array of all media files
    // param apiKey: the OpenAI API key
    // returns the full narration text, or an error message on failure
    public String generateNarrationScript(MediaFile[] media, String apiKey, String mapPhrase) {
        try {
            String url = OPENAI_BASE_URL + "/chat/completions";

            // Build multimodal content: instruction text + one image block per photo
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("[");
            contentBuilder.append("{\"type\":\"text\",\"text\":\"");
            contentBuilder.append(escapeJson(
                    "You are describing a sequence of photos and videos for a narrated slideshow. " +
                            "For each image provided, write exactly 1 to 2 SHORT sentences describing only what is " +
                            "literally visible in that image: the real subjects, location, or activity shown. " +
                            "IMPORTANT: each description must be short enough to be read aloud in exactly 7 seconds " +
                            "(roughly 15 to 20 words maximum per image). " +
                            "Do not invent feelings, backstories, or context that are not visible. " +
                            "Do not use poetic language or assume what the photographer was thinking. " +
                            "Number each description to match the image order (e.g. '1. ...', '2. ...'). " +
                            "After the last image, add this exact phrase as the final closing line of the narration: \"" + mapPhrase + "\""
            ));
            contentBuilder.append("\"}");

            // Attach AiImage.png first — it's the opening slide and needs narration too
            int imagesAdded = 0;
            File aiImage = new File("AiImage.png");
            if (aiImage.exists() && aiImage.length() < 20 * 1024 * 1024) {
                String base64 = encodeImageToBase64(aiImage);
                contentBuilder.append(",{\"type\":\"image_url\",\"image_url\":{\"url\":\"");
                contentBuilder.append("data:image/jpeg;base64,");
                contentBuilder.append(base64);
                contentBuilder.append("\",\"detail\":\"low\"}}");
                imagesAdded++;
            }

            // Attach each user media file in order — extract a frame first if it's a video
            for (MediaFile m : media) {
                File imgFile;
                boolean isTemp = false;

                if (SortMedia.forVideo(m.mediaPath)) {
                    imgFile = extractVideoFrame(new File(m.mediaPath));
                    isTemp = true;
                } else {
                    imgFile = new File(m.mediaPath);
                }

                if (imgFile != null && imgFile.exists() && imgFile.length() < 20 * 1024 * 1024) {
                    String base64 = encodeImageToBase64(imgFile);
                    contentBuilder.append(",{\"type\":\"image_url\",\"image_url\":{\"url\":\"");
                    contentBuilder.append("data:image/jpeg;base64,");
                    contentBuilder.append(base64);
                    contentBuilder.append("\",\"detail\":\"low\"}}");
                    imagesAdded++;
                }

                if (isTemp && imgFile != null) imgFile.delete();
                if (imagesAdded >= 10) break; // cap at 10 images to stay within token limits
            }

            // If no images could be sent (all files are videos), fall back to metadata-based narration
            if (imagesAdded == 0) {
                return generateNarrationFromMetadata(media, apiKey, mapPhrase);
            }

            contentBuilder.append("]");

            String requestBody = "{"
                    + "\"model\": \"gpt-4o\","
                    + "\"messages\": [{"
                    + "\"role\": \"user\","
                    + "\"content\": " + contentBuilder.toString()
                    + "}],"
                    + "\"max_tokens\": 800"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                System.out.println("    Narration generation failed. Status: " + response.statusCode());
                return generateNarrationFromMetadata(media, apiKey, mapPhrase);
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Fallback narration used only when all media files are videos (no images to send to Vision).
    // Describes each file based on its date and GPS location — kept factual and minimal.
    // param media: the sorted array of all media files
    // param apiKey: the OpenAI API key
    // returns a narration script based on available metadata
    private String generateNarrationFromMetadata(MediaFile[] media, String apiKey, String mapPhrase) {
        try {
            String url = OPENAI_BASE_URL + "/chat/completions";

            StringBuilder prompt = new StringBuilder();
            prompt.append("Write a short, factual narration for a video slideshow. ");
            prompt.append("For each entry below, write 1 sentence describing the location and date. ");
            prompt.append("IMPORTANT: each sentence must be short enough to be read aloud in 7 seconds or less ");
            prompt.append("(roughly 15 to 20 words maximum per entry). ");
            prompt.append("Do not invent details beyond what the date and GPS coordinates suggest. ");
            prompt.append("Number each line (e.g. '1. ...', '2. ...'). ");
            prompt.append("End with this exact phrase as the final closing line: \"").append(mapPhrase).append("\"\n\n");

            for (int i = 0; i < media.length; i++) {
                prompt.append("File ").append(i + 1).append(": ")
                      .append("Date=").append(media[i].date).append(", ")
                      .append("GPS=").append(media[i].GPS).append("\n");
            }

            String requestBody = "{"
                    + "\"model\": \"gpt-4o\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapeJson(prompt.toString()) + "\"}],"
                    + "\"max_tokens\": 600"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                return "Narration could not be generated.";
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Sends a single short prompt to GPT-4o and returns the raw text reply.
    // Used specifically for the map inspirational phrase, where we want a concise output
    // rather than the full multi-file narration format.
    // param prompt: the exact prompt to send
    // param apiKey: the OpenAI API key
    // returns the generated text, or an error message on failure
    public String generateText(String prompt, String apiKey) {
        try {
            String url = OPENAI_BASE_URL + "/chat/completions";

            String requestBody = "{"
                    + "\"model\": \"gpt-4o\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                System.out.println("    Text generation failed. Status: " + response.statusCode());
                return "Phrase could not be generated.";
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    // TEXT-TO-SPEECH (TTS-1)

    // Converts the narration script to audio using OpenAI TTS-1 and saves it as an MP3.
    // Uses the "alloy" voice which is neutral and clear for travel narration.
    // param narrationText: the full script to convert
    // param apiKey: the OpenAI API key
    // param outputPath: where to save the MP3 file
    public void TextToSpeech(String narrationText, String apiKey, String outputPath) {
        try {
            String url = OPENAI_BASE_URL + "/audio/speech";

            String requestBody = "{"
                    + "\"model\": \"tts-1\","
                    + "\"input\": \"" + escapeJson(narrationText) + "\","
                    + "\"voice\": \"alloy\""  // the best voice of chat
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // TTS returns raw audio bytes directly, not wrapped in JSON
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                Files.write(Paths.get(outputPath), response.body()); // save the MP3 bytes directly to disk
                System.out.println("    Audio saved: " + outputPath);
            } else {
                System.out.println("    TTS generation failed. Status: " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("    Error generating audio: " + e.getMessage());
        }
    }


    // HELPERS

    // Escapes a string so it can be safely embedded inside a JSON string value.
    // Handles quotes, backslashes, and newlines that would break the JSON payload.
    // param text: the raw string to escape
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    // Extracts the base64-encoded image data from a DALL-E 3 b64_json response.
    // DALL-E sometimes formats as "b64_json": "data" with a space after the colon.
    // use the char-by-char walker to avoid empty-string crashes.
    // param responseBody: the raw JSON string returned by the API
    // returns the base64: image string, or empty string on failure
    private String extractBase64Image(String responseBody) {
        // Try with space first (most common in recent OpenAI responses), then without
        String marker = "\"b64_json\": \"";
        if (responseBody.indexOf(marker) == -1) {
            marker = "\"b64_json\":\""; // fallback without space
        }

        int start = responseBody.indexOf(marker);
        if (start == -1) {
            System.out.println("    Could not find b64_json marker in response.");
            return "";
        }
        start += marker.length(); // move past the marker to the first char of the base64 data

        // Walk forward to find the closing quote — base64 strings never contain backslashes,
        // so we just need to find the next unescaped quote character
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (c == '\"') break; // end of the base64 string value
            sb.append(c);
        }
        return sb.toString();
    }

    // Extracts the plain text content from a GPT-4o chat completions response.
    // GPT-4o wraps the reply inside choices[0].message.content as a JSON string.
    // it walks char by char to find the real closing quote, skipping any escaped
    // sequences (\", \n, \\) that are part of the content value itself.
    // A naive indexOf("\"}", start) breaks when the content ends with a letter
    // before the brace, causing the Range out of bounds error we saw at runtime.
    // param responseBody: the raw JSON string returned by the API
    // returns the generated text string, or an error message if parsing fails
    private String extractTextFromResponse(String responseBody) {
        try {
            String marker = "\"content\": \""; // try with space first
            if (responseBody.indexOf(marker) == -1) {
                marker = "\"content\":\""; // fallback without space
            }
            int start = responseBody.indexOf(marker);
            if (start == -1) return "Could not parse response."; // marker not found

            start += marker.length(); // move past the marker to the first char of the value

            // Walk forward char by char to find the real end of the JSON string value
            StringBuilder sb = new StringBuilder();
            int i = start;
            while (i < responseBody.length()) {
                char c = responseBody.charAt(i);
                if (c == '\\' && i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    if (next == '\"') { sb.append('\"'); i += 2; continue; } // unescape \" → "
                    if (next == 'n')   { sb.append('\n'); i += 2; continue; } // unescape \n → newline
                    if (next == '\\') { sb.append('\\'); i += 2; continue; } // unescape \\ → \
                }
                if (c == '\"') break; // unescaped quote = real end of the value
                sb.append(c);
                i++;
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}