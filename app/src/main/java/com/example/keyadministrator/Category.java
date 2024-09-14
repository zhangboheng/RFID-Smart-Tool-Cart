package com.example.keyadministrator;

public class Category {
    private int id;
    private int index;
    private String typeName;
    private String typeUnit;
    private String typeImage;

    // 构造函数
    public Category(int id, int index, String typeName, String typeUnit, String typeImage) {
        this.id = id;
        this.index = index;
        this.typeName = typeName;
        this.typeUnit = typeUnit;
        this.typeImage = typeImage;
    }

    // 重新构造函数
    public Category(int index, String typeName, String typeUnit, String typeImage) {
        this.index = index;
        this.typeName = typeName;
        this.typeUnit = typeUnit;
        this.typeImage = typeImage;
    }

    // Getter 方法
    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeUnit() {
        return typeUnit;
    }

    public String getTypeImage() {return typeImage;}

    public void setId(int id) {
        this.id = id;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeUnit(String typeUnit) {
        this.typeUnit = typeUnit;
    }

    public void setTypeImage(String typeImage) {this.typeImage = typeImage;}

}