package com.example.keyadministrator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Base64ToImage {

    public static void convertBase64ToImage(String base64Image, String filePath) {
        // 解码 Base64 字符串
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

        // 创建 Bitmap 对象
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // 保存 Bitmap 到本地文件
        FileOutputStream fos = null;
        try {File imageFile = new File(filePath);
            fos = new FileOutputStream(imageFile);
            decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String saveBase64ToLocal(String base64String) {
        // 解码 Base64 字符串
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

        // 创建 Bitmap 对象
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // 获取图片存储目录
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // 创建图片文件
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix*/
                    storageDir/* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 保存 Bitmap 到本地文件
        if (imageFile != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                return imageFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static File saveBase64ToFile(String base64String) {
        // 解码 Base64 字符串
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

        // 创建 Bitmap 对象
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // 获取图片存储目录
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // 创建图片文件
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix*/
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 保存 Bitmap 到本地文件
        if (imageFile != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                return imageFile; // 返回文件对象
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }}
        return null; // 保存失败，返回 null
    }
}
