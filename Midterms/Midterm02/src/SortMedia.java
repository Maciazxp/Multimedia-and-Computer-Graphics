import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

// Utility class for sorting and querying a MediaFile array.
// All methods are static since no instance state is needed.
public class SortMedia {

    // Parses the date string from EXIF format into a LocalDateTime for comparison.
    // Falls back to LocalDateTime.MIN if the string is missing or malformed,
    // so files without date data still sort (they go to the front).
    // param dateStr: the raw date string from exiftool
    private static LocalDateTime extractDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            return LocalDateTime.MIN; // fallback so sorting never crashes on bad data
        }
    }

    // Sorts the media array in place from oldest to newest capture date.
    // param media is the array to sort (modified directly)
    public static void sortByDate(MediaFile[] media) {
        Arrays.sort(media, Comparator.comparing(m -> extractDate(m.date)));
    }

    // Returns the oldest file in the array.
    // Assumes the array has already been sorted by sortByDate().
    // param media: is the sorted array
    public static MediaFile getOldest(MediaFile[] media) {
        return media[0]; // first element after sorting = oldest
    }

    // Returns the newest file in the array.
    // Assumes the array has already been sorted by sortByDate().
    // param media: is the sorted array
    public static MediaFile getNewest(MediaFile[] media) {
        return media[media.length - 1]; // last element after sorting = newest
    }

    // Checks whether the given file path points to a video based on its extension.
    // param path: the file path to check
    public static boolean forVideo(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".mp4") || p.endsWith(".mov") || p.endsWith(".avi")
                || p.endsWith(".mkv") || p.endsWith(".webm") || p.endsWith(".m4v");
    }
}
