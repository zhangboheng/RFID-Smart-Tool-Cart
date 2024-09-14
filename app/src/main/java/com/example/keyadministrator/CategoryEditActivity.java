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

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CategoryEditActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 前置摄像头
    private final DatabaseHelper dbHelper = new DatabaseHelper(this);
    private int categoryId;
    private String imageGlobalPath = "";
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        socketClient = SocketClient.getInstance();

        // 返回按钮点击事件
        backButton();
        // 设置图片点击监听器
        setImageViewClickListener();
        // 取消按钮点击事件
        cancelButton();
        // 从上一层获取初始数据
        initialData();
        // 点击确认按钮后进行保存事件
        confirmButton();

    }

    // 返回按钮点击事件
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

    // 取消按钮点击事件
    private void cancelButton() {
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(v -> finish());
    }

    // 从上一层获取初始数据
    private void initialData() {
        EditText editTextIndex = findViewById(R.id.editTextIndex);
        EditText editTextTypeName = findViewById(R.id.editTextTypeName);
        EditText editTextTypeUnit = findViewById(R.id.editTextTypeUnit);
        ImageView categoryImageView = findViewById(R.id.imageViewCategory);
        // 获取从前一个 Activity 传递过来的分类数据
        Intent intent = getIntent();
        if (intent != null) {
            categoryId = intent.getIntExtra("categoryId", -1);
            int index = intent.getIntExtra("index", -1);
            String typeName = intent.getStringExtra("typeName");
            String typeUnit = intent.getStringExtra("typeUnit");
            String imagePath = dbHelper.getCategoryById(categoryId).getTypeImage();
            if (index != -1) {
                editTextIndex.setText(String.valueOf(index));
                editTextTypeName.setText(typeName);
                editTextTypeUnit.setText(typeUnit);
                if (imagePath != null && !imagePath.isEmpty()) {
                    Glide.with(this).load(imagePath).into(categoryImageView);
                    this.imageGlobalPath = imagePath;
                }
            }
        }
    }

    // 点击确认按钮后进行保存事件
    private void confirmButton() {
        EditText editTextIndex = findViewById(R.id.editTextIndex);
        EditText editTextTypeName = findViewById(R.id.editTextTypeName);
        EditText editTextTypeUnit = findViewById(R.id.editTextTypeUnit);
        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(v -> {
            String indexStr = editTextIndex.getText().toString().trim();
            String typeName = editTextTypeName.getText().toString().trim();
            String typeUnit = editTextTypeUnit.getText().toString().trim();

            if (indexStr.isEmpty() || typeName.isEmpty() || typeUnit.isEmpty()) {
                Toast.makeText(CategoryEditActivity.this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int index = Integer.parseInt(indexStr);
                    // 创建 Category 对象
                    Category category = new Category(categoryId, index, typeName, typeUnit, imageGlobalPath);
                    // 更新数据库中的分类数据
                    int rowsAffected = dbHelper.updateCategory(category);
                    if (rowsAffected > 0) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            // 创建内部data JSON对象并添加Ids数组
                            JSONObject innerData = new JSONObject();
                            innerData.put("categoryId", categoryId);
                            innerData.put("categoryName", typeName);
                            innerData.put("orderNo", index);
                            String imagePath = imageGlobalPath;
                            String base64String = ImageUtils.imageToBase64(imagePath);
                            innerData.put("categoryImage", base64String);
                            innerData.put("unit", typeUnit);
                            if (base64String != null) {
                                // 使用 base64String，例如发送到服务器
                                socketClient.sendParameterData(dateFormat.format(new Date()), "update_goods_category", innerData.toString());
                            } else {
                                Toast.makeText(CategoryEditActivity.this, "图片错误", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(CategoryEditActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPreferences = getSharedPreferences("editCategorySuccess", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("paramName", "success"); // 保存参数
                        editor.apply();
                        finish();
                    } else {
                        Toast.makeText(CategoryEditActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(CategoryEditActivity.this, "请输入有效的索引序号", Toast.LENGTH_SHORT).show();
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
            Bitmap imageBitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
            imageViewCategory.setImageBitmap(imageBitmap);

            // 保存图片到本地并获取路径
            imageGlobalPath = saveImageToLocal(Objects.requireNonNull(imageBitmap));
        }
    }

    private String saveImageToLocal(Bitmap imageBitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        if (!imageGlobalPath.isEmpty()) {
            imageFile = new File(imageGlobalPath); // 使用原图片路径
        }else{
            try {
                imageFile = File.createTempFile(imageFileName,  /* prefix */
                        ".jpg",         /* suffix*/
                        storageDir      /* directory */);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        return null;
    }

    // 页面消失后事件
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        imageGlobalPath = "";
        dbHelper.close();
    }
}
