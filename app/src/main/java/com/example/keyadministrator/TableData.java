package com.example.keyadministrator;

public class TableData {
    public int imageResId;
    public String imagePath;
    public String text;
    public String storage;
    public String lend;
    public String total;
    public String detail;
    public String belongs;

    public TableData(int imageResId, String text, String storage, String lend, String total, String detail, String imagePath, String belongs) {
        this.imageResId = imageResId;
        this.text = text;
        this.storage = storage;
        this.lend = lend;
        this.total = total;
        this.detail = detail;
        this.imagePath = imagePath;
        this.belongs = belongs;
    }
    public String getText() {return text;}

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getBelongs() { return belongs; }
}
