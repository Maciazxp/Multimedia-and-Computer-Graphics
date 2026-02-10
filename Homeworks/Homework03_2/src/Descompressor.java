import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Descompressor {
    public void decompress(String inputPath, String outputPath) throws IOException {

        try (DataInputStream in = new DataInputStream(new FileInputStream(inputPath))) {
            // read header
            short magic = in.readShort();
            if ((magic & 0xFFFF) != 0xCAFE) {
                throw new IOException("invalid" + Integer.toHexString(magic & 0xFFFF));
            }

            byte version = in.readByte();
            int width = in.readInt();
            int height = in.readInt();
            int paletteSize = in.readUnsignedByte();

            //read palette
            int[] palette = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) {
                palette[i] = in.readInt();
            }

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);


            int pixelIndex = 0;
            while (pixelIndex < width * height) {
                int count = in.readUnsignedByte();
                int colorIndex = in.readUnsignedByte();
                int color = palette[colorIndex];

                for (int i = 0; i < count; i++) {
                    int x = pixelIndex % width;
                    int y = pixelIndex / width;
                    img.setRGB(x, y, color);
                    pixelIndex++;
                }
            }
            ImageIO.write(img, "JPG", new File(outputPath));
        }
    }
}
