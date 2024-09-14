package com.example.keyadministrator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CategoryManagementActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 前置摄像头
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    private String imagePath = "";
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        socketClient = SocketClient.getInstance();

        // 设置返回事件
        backButton();
        // 设置图片点击监听器
        setImageViewClickListener();
        // 设置点击取消返回事件
        cancelButton();
        // 设置点击确定按钮返回事件
        confirmButton();
    }

    // 设置点击按钮返回事件
    private void backButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    // 设置图片点击监听器
    private void setImageViewClickListener() {
        ImageView imageViewCategory = findViewById(R.id.imageViewCategory);
        imageViewCategory.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    // 设置点击取消返回事件
    private void cancelButton() {
        Button cancelButton = findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(v -> finish());
    }

    // 设置点击确定按钮返回事件
    private void confirmButton() {
        EditText editTextTypeName = findViewById(R.id.editTextTypeName);
        EditText editTextTypeUnit = findViewById(R.id.editTextTypeUnit);
        Button confirmButton = findViewById(R.id.buttonConfirm);
        confirmButton.setOnClickListener(v -> {
            String indexStr = "1";
            String typeName = editTextTypeName.getText().toString().trim();
            String typeUnit = editTextTypeUnit.getText().toString().trim();
            String typeImage = imagePath;
            if (typeName.isEmpty() || typeUnit.isEmpty() || typeImage.isEmpty()) {
                Toast.makeText(CategoryManagementActivity.this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int index = Integer.parseInt(indexStr); // 尝试将索引序号字符串转换为整数
                    long newRowId = dbHelper.addCategory(index, typeName, typeUnit, typeImage); // 添加分类管理内容条目
                    if (newRowId != -1) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            // 创建内部data JSON对象并添加Ids数组
                            JSONObject innerData = new JSONObject();
                            innerData.put("categoryName", typeName);
                            innerData.put("orderNo", index);
                            String base64String = ImageUtils.imageToBase64(typeImage);
                            innerData.put("categoryImage", base64String);
                            innerData.put("unit", typeUnit);
                            if (base64String != null) {
                                // 使用 base64String，例如发送到服务器
                                socketClient.sendParameterData(dateFormat.format(new Date()), "add_goods_category", innerData.toString());
                            } else {
                                Toast.makeText(CategoryManagementActivity.this, "图片错误", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(CategoryManagementActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPreferences = getSharedPreferences("editCategorySuccess", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("paramName", "success"); // 保存参数
                        editor.apply();
                        finish();
                    }
                } catch (NumberFormatException e) {
                    // 转换失败，提示用户输入有效的数字
                    Toast.makeText(CategoryManagementActivity.this, "请输入有效的索引序号", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 处理拍摄结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageViewCategory = findViewById(R.id.imageViewCategory);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewCategory.setImageBitmap(imageBitmap);

            // 保存图片到本地并获取路径
            imagePath = saveImageToLocal(imageBitmap);
        }
    }

    private String saveImageToLocal(Bitmap imageBitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName,  /* prefix */
                    ".jpg",         /* suffix*/
                    storageDir      /* directory */);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageFile != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        imagePath = "";
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}