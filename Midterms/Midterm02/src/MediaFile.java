// Represents a single media file (photo or video) with its extracted metadata.
// Once created, the object holds the path, GPS coordinates, and capture date
// extracted automatically via exiftool.
public class MediaFile {

    public final String mediaPath; // absolute or relative path to the file
    public String GPS;             // GPS coordinates in "lat, lon" format (or "No data")
    public String date;            // capture date in "yyyy:MM:dd HH:mm:ss" format (or "No data")

    // param path: is the path to the media file on disk
    // throws RuntimeException if path is null or empty
    public MediaFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Media file path cannot be null or empty.");
        }
        this.mediaPath = path;
        extractMetadata(path); // pull GPS and date right away on construction
    }

    // Extracts GPS and date metadata from the file using MetaExtractor.
    // If metadata is missing, the fields stay as "No data" (handled inside MetaExtractor).
    // param path: the file path passed to exiftool
    private void extractMetadata(String path) {
        Exiftool extractor = new Exiftool();
        this.GPS  = extractor.extractGPS(path);
        this.date = extractor.extractDate(path);
    }

    // Returns a short summary of this media file for logging or debugging .
    @Override
    public String toString() {
        return "MediaFile{path='" + mediaPath + "', date='" + date + "', GPS='" + GPS + "'}";
    }
}
