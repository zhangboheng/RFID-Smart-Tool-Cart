package com.example.keyadministrator;

import androidx.annotation.NonNull;

public class Notify{
    private int id;
    private String status;
    private String item;
    private String date;

    // 构造函数（可选）
    public Notify(int id, String status, String item, String date) {
        this.id = id;
        this.status = status;
        this.item = item;
        this.date = date;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return "Notify{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", item='" + item + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
