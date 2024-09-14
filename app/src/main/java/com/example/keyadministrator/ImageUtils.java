package com.example.keyadministrator;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageUtils {

    public static String imageToBase64(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            FileInputStream fis = new FileInputStream(imageFile);
            byte[] imageBytes = new byte[(int) imageFile.length()];
            fis.read(imageBytes);
            fis.close();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}