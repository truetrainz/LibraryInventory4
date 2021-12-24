import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("This is a working main class");
        Run run = new Run();
        run.getBarcode();
    }
}
