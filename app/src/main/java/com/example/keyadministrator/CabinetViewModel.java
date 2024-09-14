package com.example.keyadministrator;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CabinetViewModel extends ViewModel {

    private HashMap<String, List<FullBoxData>> cabinetData= new HashMap<>();

    public void loadCabinetData(String cabinetName, DatabaseHelper dbHelper) {
        // 提取分类需要数据
        HashMap<String, String> categoryMap = new HashMap<>();
        List<Category> categoryList = dbHelper.getAllCategories();
        for(Category name : categoryList) {
            categoryMap.put(name.getTypeName(), name.getTypeImage());
        }
        List<FullBoxData> fullItems = new ArrayList<>();
        HashMap<String, int[]> countCanMap = dbHelper.countGoodsByTypeAndBorrowStatus(cabinetName);
        for (Map.Entry<String, int[]> entry : countCanMap.entrySet()) {
            String typeName = entry.getKey();
            int[] counts = entry.getValue();
            String typeImage = categoryMap.get(typeName);
            fullItems.add(new FullBoxData(R.mipmap.test, "名称：" + typeName,"库存：" + (counts[0] - counts[1]), "借出：" + counts[1], "总计：" + counts[0], typeImage));
        }
        cabinetData.put(cabinetName, fullItems);
    }

    public List<FullBoxData> getCabinetData(String cabinetName) {
        return cabinetData.get(cabinetName);
    }
}
