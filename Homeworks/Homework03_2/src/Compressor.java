import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compressor {
    //create a reduced color palette
    private List<Integer> createColorPalette(int[] pixels, int maxColor) {
        //uses a palette of 64 colors
        List<Integer> palette = new ArrayList<>();
        for (int r = 0; r < 256; r += 64) {
            for (int g = 0; g < 256; g += 64) {
                for (int b = 0; b < 256; b += 64) {

                    int color =(r << 16) | (g << 8) | b;
                    palette.add(color);
                }
            }
        }
        return palette;
    }

    //create a black-white color palette
    private List<Integer> createBlackWhitePalette() {
        List<Integer> palette = new ArrayList<>();
        // only white (0,0,0) and black (255,255,255)
        palette.add(0x000000);        // black
        palette.add(0xFFFFFF);        // white
        palette.add(0x808080);        // dark gray
        palette.add(0xC0C0C0);        // light gray
        return palette;
    }


    //find the closest color on the palette
    private int findClosestColor(int color, List<Integer> palette) {
        int minDistance = Integer.MAX_VALUE;
        int bestIndex = 0;

        int r1 = (color >> 16) & 0xFF;
        int g1 = (color >> 8) & 0xFF;
        int b1 = color & 0xFF;

        for (int i = 0; i < palette.size(); i++) {
            int c = palette.get(i);
            int r2 = (c >> 16) & 0xFF;
            int g2 = (c >> 8) & 0xFF;
            int b2 = c & 0xFF;

            //Euclidean distance
            int distance = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);

            if (distance < minDistance) {
                minDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex; //returns the palette index
    }

    //Compress with RLE on palette indices
    public void compress(int[] pixels, int width, int height, String outputPath) throws IOException {
        //creates the palette
        List<Integer> palette = createColorPalette(pixels, width);

        //Convert pixels to palette indices
        byte[] indices = new byte[pixels.length];
        for (int i = 0; i < pixels.length; i++) {

            indices[i] = (byte) findClosestColor(pixels[i], palette);
        }

        //Apply RLE to the indices
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath))) {
            // write header
            out.writeShort(0xCAFE);
            out.writeByte(1);
            out.writeInt(width);
            out.writeInt(height);
            out.writeByte(palette.size()); // palette size

            // draw palette
            for (int color : palette) {
                out.writeInt(color);
            }

            //Compress indexes with RLE
            byte current = indices[0];
            int count = 1;

            for (int i = 1; i < indices.length; i++) {
                if (indices[i] == current && count < 255) {
                    count++;
                } else {
                    out.writeByte(count);
                    out.writeByte(current);
                    current = indices[i];
                    count = 1;
                }

            }
            out.writeByte(count);
            out.writeByte(current);
        }
    }
}
