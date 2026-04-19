import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

// Builds the closing map image
// Instead of relying on a static-map service (which can go down), this class
// assembles the map directly from individual OSM tile images fetched from
// tile.openstreetmap.org, which is the same source every OSM-based app uses.
// After assembling the tiles, it draws two distinct pins (green S = start,
// red E = end) and overlays the AI-generated inspirational phrase at the bottom.
public class MapBuilder {

    // OSM tile server — each tile is a 256x256 PNG at a given zoom/x/y address
    private static final String OSM_TILE_URL = "https://tile.openstreetmap.org";

    // Each OSM tile is always 256x256 pixels
    private static final int TILE_SIZE = 256;

    // Final map dimensions — portrait oriented to match the video frame
    private static final int MAP_WIDTH  = 1080;
    private static final int MAP_HEIGHT = 1440;

    // Pin circle radius in pixels (drawn directly with Graphics2D)
    private static final int PIN_RADIUS = 18;

    private final HttpClient httpClient;

    public MapBuilder() {
        // Timeout so a slow tile server doesn't hang the whole program
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Assembles the map from OSM tiles, draws the start and end pins on it,
    // and overlays the inspirational phrase at the bottom.
    // param oldest: the first (oldest) media file, it provides the starting GPS pin
    // param newest: the last (newest) media file, it provides the ending GPS pin
    // param phrase: the AI-generated inspirational phrase to overlay on the map
    // param outputPath: where to save the final map PNG
    public void buildMap(MediaFile oldest, MediaFile newest, String phrase, String outputPath) {
        try {
            System.out.println("    Assembling map from OSM tiles...");

            // If neither file has GPS, we cannot build a real map — skip gracefully
            if (oldest.GPS.equals("No data") && newest.GPS.equals("No data")) {
                System.out.println("    WARNING: No GPS data found in any file. Using fallback image.");
                createFallbackMapImage(phrase, outputPath);
                return;
            }

            // If only one file has GPS, reuse it for both pins (they overlap but won't crash)
            String startGPS = oldest.GPS.equals("No data") ? newest.GPS : oldest.GPS;
            String endGPS   = newest.GPS.equals("No data") ? oldest.GPS : newest.GPS;

            // Parse GPS strings into decimal lat/lon
            double[] startCoords = parseDMSToDecimal(startGPS);
            double[] endCoords   = parseDMSToDecimal(endGPS);

            // Center the map at the midpoint between the two pins
            double centerLat = (startCoords[0] + endCoords[0]) / 2.0;
            double centerLon = (startCoords[1] + endCoords[1]) / 2.0;

            // Pick a zoom level that keeps both pins visible in the frame
            int zoom = computeZoom(startCoords, endCoords);
            System.out.println("    Center: " + centerLat + ", " + centerLon + " | Zoom: " + zoom);

            // Assemble the map canvas from individual OSM tiles
            BufferedImage mapImage = assembleTiles(centerLat, centerLon, zoom);

            if (mapImage == null) {
                System.out.println("    Tile assembly failed. Using fallback image.");
                createFallbackMapImage(phrase, outputPath);
                return;
            }

            // Convert lat/lon to pixel positions on the assembled canvas
            int[] startPixel = latLonToPixel(startCoords[0], startCoords[1], centerLat, centerLon, zoom);
            int[] endPixel   = latLonToPixel(endCoords[0],   endCoords[1],   centerLat, centerLon, zoom);

            // Draw the two pins directly onto the map
            drawPin(mapImage, startPixel[0], startPixel[1], Color.GREEN, "S"); // S = Start
            drawPin(mapImage, endPixel[0],   endPixel[1],   Color.RED,   "E"); // E = End

            // Overlay the inspirational phrase at the bottom
            overlayPhrase(mapImage, phrase);

            // Save the final composite image
            ImageIO.write(mapImage, "png", new File(outputPath));
            System.out.println("    Map image saved: " + outputPath);

        } catch (Exception e) {
            System.out.println("    Error building map: " + e.getMessage());
            // Even if the map fails, try to at least save the fallback so the video can continue
            createFallbackMapImage(phrase, outputPath);
        }
    }

    // TILE ASSEMBLY

    // Fetches the OSM tiles needed to cover MAP_WIDTH x MAP_HEIGHT around the center point
    // and stitches them into a single BufferedImage.
    // param centerLat: latitude of the map center
    // param centerLon: longitude of the map center
    // param zoom: the OSM zoom level to use
    // returns the assembled map image, or null if all tile fetches fail
    private BufferedImage assembleTiles(double centerLat, double centerLon, int zoom) {
        // Convert center lat/lon to the fractional tile coordinates at this zoom level
        double centerTileX = lonToTileX(centerLon, zoom);
        double centerTileY = latToTileY(centerLat, zoom);

        // How many tiles we need to cover the canvas (add 2 for safety margin at the edges)
        int tilesX = (int) Math.ceil((double) MAP_WIDTH  / TILE_SIZE) + 2;
        int tilesY = (int) Math.ceil((double) MAP_HEIGHT / TILE_SIZE) + 2;

        // Top-left tile index of the region we need to download
        int startTileX = (int) centerTileX - tilesX / 2;
        int startTileY = (int) centerTileY - tilesY / 2;

        // Pixel offset so the center coordinate lands exactly in the middle of the canvas
        int offsetX = (int) ((centerTileX - startTileX) * TILE_SIZE) - MAP_WIDTH  / 2;
        int offsetY = (int) ((centerTileY - startTileY) * TILE_SIZE) - MAP_HEIGHT / 2;

        // Create the output canvas
        BufferedImage canvas = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(new Color(200, 200, 200)); // light grey fallback for any missing tiles
        g.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        int maxTile = (int) Math.pow(2, zoom); // tiles wrap around at the antimeridian
        boolean anyTileLoaded = false;

        for (int tx = 0; tx < tilesX; tx++) {
            for (int ty = 0; ty < tilesY; ty++) {
                int tileX = (startTileX + tx) % maxTile; // wrap tile X around the antimeridian
                int tileY =  startTileY + ty;

                // Skip tiles outside valid latitude range (OSM tiles only go from y=0 to 2^zoom-1)
                if (tileX < 0 || tileY < 0 || tileY >= maxTile) continue;

                BufferedImage tile = fetchTile(zoom, tileX, tileY);
                if (tile != null) {
                    // Paint the tile at the right position on the canvas
                    int drawX = tx * TILE_SIZE - offsetX;
                    int drawY = ty * TILE_SIZE - offsetY;
                    g.drawImage(tile, drawX, drawY, null);
                    anyTileLoaded = true;
                }
            }
        }

        g.dispose();
        return anyTileLoaded ? canvas : null; // return null only if every single tile failed
    }

    // Fetches a single OSM tile image from tile.openstreetmap.org.
    // OSM requires a descriptive User-Agent to avoid being rate-limited.
    // param zoom: the zoom level
    // param x: the tile column index
    // param y: the tile row index
    // returns the tile as a BufferedImage, or null if the fetch fails
    private BufferedImage fetchTile(int zoom, int x, int y) {
        try {
            String url = OSM_TILE_URL + "/" + zoom + "/" + x + "/" + y + ".png";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TravelVideoCreator/1.0 (student project)") // OSM requires a real User Agent xd
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                return ImageIO.read(response.body()); // parse the PNG tile directly from the stream
            }
        } catch (Exception e) {
            // A single tile failing is not fatal — the canvas shows grey for that spot
            System.out.println("    Tile fetch failed (" + zoom + "/" + x + "/" + y + "): " + e.getMessage());
        }
        return null;
    }


    // COORDINATE CONVERSIONS

    // Converts longitude to a fractional OSM tile X coordinate at the given zoom level.
    // param lon: longitude in decimal degrees
    // param zoom: OSM zoom level
    private double lonToTileX(double lon, int zoom) {
        return (lon + 180.0) / 360.0 * Math.pow(2, zoom);
    }

    // Converts latitude to a fractional OSM tile Y coordinate at the given zoom level.
    // Uses the Web Mercator projection that OSM uses internally.
    // param lat: latitude in decimal degrees
    // param zoom: OSM zoom level
    private double latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI)
               / 2.0 * Math.pow(2, zoom);
    }

    // Converts a lat/lon coordinate to a pixel position on the assembled canvas.
    // param lat: latitude of the point
    // param lon: longitude of the point
    // param centerLat: latitude of the canvas center
    // param centerLon: longitude of the canvas center
    // param zoom: OSM zoom level (must match the one used in assembleTiles)
    // returns int[] { pixelX, pixelY } relative to the top-left of the canvas
    private int[] latLonToPixel(double lat, double lon, double centerLat, double centerLon, int zoom) {
        double pointX  = lonToTileX(lon, zoom)  * TILE_SIZE;
        double pointY  = latToTileY(lat, zoom)  * TILE_SIZE;
        double centerX = lonToTileX(centerLon, zoom) * TILE_SIZE;
        double centerY = latToTileY(centerLat, zoom) * TILE_SIZE;

        // Offset from center in world pixels, then shift to canvas pixel coordinates
        int pixelX = (int) ((pointX - centerX) + MAP_WIDTH  / 2.0);
        int pixelY = (int) ((pointY - centerY) + MAP_HEIGHT / 2.0);

        return new int[]{ pixelX, pixelY };
    }


    // PIN AND PHRASE DRAWING

    // Draws a colored circular pin with a letter label at the given pixel position.
    // The white border ensures the pin is visible against any map color underneath.
    // param image: the map image to draw on (modified in place)
    // param x: the horizontal pixel position of the pin center
    // param y: the vertical pixel position of the pin center
    // param color: the fill color of the pin
    // param label a single letter shown inside the pin like "S" or "E"
    private void drawPin(BufferedImage image, int x, int y, Color color, String label) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // White border so the pin stands out against any map color underneath
        g.setColor(Color.WHITE);
        g.fillOval(x - PIN_RADIUS - 2, y - PIN_RADIUS - 2, (PIN_RADIUS + 2) * 2, (PIN_RADIUS + 2) * 2);

        // Colored fill
        g.setColor(color);
        g.fillOval(x - PIN_RADIUS, y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);

        // Letter label centered inside the pin
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, PIN_RADIUS));
        FontMetrics fm = g.getFontMetrics();
        int labelX = x - fm.stringWidth(label) / 2;
        int labelY = y + fm.getAscent() / 2 - 2; // slight upward nudge to look visually centered
        g.drawString(label, labelX, labelY);

        g.dispose();
    }

    // Draws the inspirational phrase over a semi-transparent dark banner at the bottom of the map.
    // The banner ensures the text is readable regardless of the map colors underneath.
    // param image: the map image to draw on (modified in place)
    // param phrase: the text to render


    private void overlayPhrase(BufferedImage image, String phrase) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int imgWidth  = image.getWidth();
        int imgHeight = image.getHeight();

        g.setFont(new Font("SansSerif", Font.ITALIC, 36));
        FontMetrics fm = g.getFontMetrics();

        // Split the phrase into lines that fit within the image width (with padding)
        int maxLineWidth = imgWidth - 80; // 40px padding on each side
        List<String> lines = wrapText(phrase, fm, maxLineWidth);

        // Resize banner to fit however many lines we have
        int lineHeight  = fm.getHeight();
        int bannerHeight = Math.max(imgHeight / 5, lines.size() * lineHeight + 40); // min 20% height
        int bannerY     = imgHeight - bannerHeight;

        // Semi-transparent black banner
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, bannerY, imgWidth, bannerHeight);

        // Draw each line centered in the banner
        g.setColor(Color.WHITE);
        int totalTextHeight = lines.size() * lineHeight;
        int startY = bannerY + (bannerHeight - totalTextHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int textX = (imgWidth - fm.stringWidth(line)) / 2;
            g.drawString(line, textX, startY);
            startY += lineHeight;
        }

        g.dispose();
    }

    // Breaks a phrase into lines that fit within maxWidth pixels.
// Splits on word boundaries so no word gets cut in half.
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) lines.add(currentLine.toString());
                currentLine = new StringBuilder(word); // start new line with this word
            }
        }

        if (!currentLine.isEmpty()) lines.add(currentLine.toString()); // add the last line
        return lines;
    }


    // GPS PARSING

    // Parses a DMS GPS string from exiftool into decimal degrees.
    // exiftool returns strings like: "43 deg 28' 1.49\" N, 11 deg 53' 4.34\" E"
    // Returns double[] { latitude, longitude } in decimal degrees.
    // South latitudes and West longitudes are negative in decimal notation.
    // param gps the raw GPS string from exiftool
    private double[] parseDMSToDecimal(String gps) {
        try {
            String[] parts = gps.split(","); // split into lat and lon halves
            double lat = parseSingleDMS(parts[0].trim());
            double lon = parseSingleDMS(parts[1].trim());
            return new double[]{ lat, lon };
        } catch (Exception e) {
            return new double[]{ 0.0, 0.0 }; // fallback to null island if parsing fails
        }
    }

    // Parses one half of a DMS coordinate string (latitude or longitude).
    // Strips degree symbols, apostrophes, and quote marks before tokenizing.
    // param dms: a string
    // returns the decimal degree value (negative for S or W)
    private double parseSingleDMS(String dms) {
        dms = dms.replaceAll("[^0-9.NSEW ]", " ").trim(); // strip non-numeric symbols
        String[] tokens = dms.split("\\s+");

        double degrees   = Double.parseDouble(tokens[0]);
        double minutes   = Double.parseDouble(tokens[1]);
        double seconds   = Double.parseDouble(tokens[2]);
        String direction = tokens[3].toUpperCase();

        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);

        // South and West are negative in decimal degree notation
        if (direction.equals("S") || direction.equals("W")) {
            decimal = -decimal;
        }

        return decimal;
    }


    // ZOOM AND FALLBACK

    // Picks an OSM zoom level that keeps both GPS pins visible in the frame.
    // The farther apart the two points, the lower the zoom (more zoomed out).
    // param start decimal { lat, lon } of the start location
    // param end   decimal { lat, lon } of the end location
    // returns a zoom level between 3 (continent) and 15 (street)
    private int computeZoom(double[] start, double[] end) {
        double latDiff = Math.abs(start[0] - end[0]);
        double lonDiff = Math.abs(start[1] - end[1]);
        double maxDiff = Math.max(latDiff, lonDiff);

        if (maxDiff > 40)  return 3;   // continental distance
        if (maxDiff > 15)  return 4;   // cross-country
        if (maxDiff > 6)   return 5;   // regional
        if (maxDiff > 2)   return 7;   // neighboring cities
        if (maxDiff > 0.5) return 10;  // same city, different neighborhoods
        return 14;                     // very close — street level
    }

    // Creates a plain dark image with the phrase when no GPS data is available,
    // or when the tile fetch fails completely.
    // This ensures the closing slide always exists so the video can be assembled.
    // param phrase: the AI-generated phrase to display
    // param outputPath: where to save the fallback image
    private void createFallbackMapImage(String phrase, String outputPath) {
        try {
            BufferedImage fallback = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = fallback.createGraphics();

            // Dark background as a substitute for the map
            g.setColor(new Color(30, 30, 50));
            g.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

            // Centered "no location" label
            g.setColor(new Color(150, 150, 170));
            g.setFont(new Font("SansSerif", Font.PLAIN, 28));
            String noGps = "No location data available";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(noGps, (MAP_WIDTH - fm.stringWidth(noGps)) / 2, MAP_HEIGHT / 2);

            g.dispose();

            // Still overlay the inspirational phrase at the bottom
            overlayPhrase(fallback, phrase);

            ImageIO.write(fallback, "png", new File(outputPath));
            System.out.println("    Fallback map image saved: " + outputPath);
        } catch (Exception e) {
            System.out.println("    Could not create fallback map: " + e.getMessage());
        }
    }
}
