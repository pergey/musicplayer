package com.example.musicplayer5;

import com.google.gson.Gson;
import javafx.scene.image.Image;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Utilities {
    static public void downloadFile(String fileUrl, File file) {
        try {
            URL url = new URL(fileUrl);
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            in.close();
            fileOutputStream.close();

            System.out.println("File downloaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image downloadImage(String imageUrl) {
        try {
            // Download the image from the URL
            InputStream inputStream = new URL(imageUrl).openStream();

            // Create an Image object asynchronously
            Image image = new Image(inputStream);

            // Close the input stream
            inputStream.close();

            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    public static String makeHttpRequest(String urlString) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
