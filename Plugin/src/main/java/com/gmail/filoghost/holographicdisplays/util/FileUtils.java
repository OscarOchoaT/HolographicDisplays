package com.gmail.filoghost.holographicdisplays.util;

import com.gmail.filoghost.holographicdisplays.exception.UnreadableImageException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FileUtils {

    public static List<String> readLines(File file) throws IOException, Exception {

        if (!file.isFile()) {
            throw new FileNotFoundException(file.getName());
        }

        BufferedReader br = null;

        try {

            List<String> lines = new ArrayList<String>();

            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();

            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }

            return lines;

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static BufferedImage readImage(File file) throws UnreadableImageException, IOException, Exception {

        if (!file.isFile()) {
            throw new FileNotFoundException(file.getName());
        }

        BufferedImage image = ImageIO.read(file);

        if (image == null) {
            throw new UnreadableImageException();
        }

        return image;
    }

    public static BufferedImage readImage(URL url) throws UnreadableImageException, IOException, Exception {

        BufferedImage image = ImageIO.read(url);

        if (image == null) {
            throw new UnreadableImageException();
        }

        return image;
    }
}
