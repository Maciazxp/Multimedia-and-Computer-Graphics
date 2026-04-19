import java.io.BufferedReader;
import java.io.InputStreamReader;

// Handles metadata extraction from media files using exiftool.
// Reads GPS coordinates and capture date from EXIF data embedded in photos and videos.
public class Exiftool {

    // Extracts the GPS position from the file's EXIF data.
    // Returns the GPS string or "No data" if not found.
    // param path: the path to the media file
    public String extractGPS(String path) {
        return runExiftool(path, "-GPSPosition");
    }

    // Extracts the capture date from the file's EXIF data.
    // Looks for CreateDate first
    // Returns a date string in "yyyy:MM:dd HH:mm:ss" format or "No data".
    // param path: the path to the media file
    public String extractDate(String path) {
        return runExiftool(path, "-CreateDate");
    }

    // Runs exiftool with the given tag and returns the raw value.
    // Uses -s3 flag to get only the value with no label or formatting overhead.
    // param path: the file to inspect
    // param tag: the exiftool tag to extract
    // returns the trimmed tag value, "No data" if empty, or an error message on failure
    private String runExiftool(String path, String tag) {
        ProcessBuilder pb = new ProcessBuilder("exiftool", "-s3", tag, path);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String result = reader.readLine(); // exiftool outputs one line per tag with -s3

            process.waitFor();
            return (result != null) ? result.trim() : "No data"; // signal missing data clearly
        } catch (Exception e) {
            return "Error: " + e.getMessage(); // in case exiftool is missing or the file is unreadable
        }
    }
}
