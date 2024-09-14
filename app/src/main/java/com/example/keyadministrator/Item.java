package com.example.keyadministrator;

public class Item {
    private final String userName;
    private final String statue;
    private final String operationType;
    private final String cabinetNumber;
    private final String time;
    private final String avatarResId;

    // 构造函数
    public Item(String userName, String statue, String operationType, String cabinetNumber, String time, String avatarResId) {
        this.userName = userName;
        this.statue = statue;
        this.operationType = operationType;
        this.cabinetNumber = cabinetNumber;
        this.time = time;
        this.avatarResId = avatarResId;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getStatue() { return statue; }

    public String getOperationType() {
        return operationType;
    }

    public String getCabinetNumber() {
        return cabinetNumber;
    }

    public String getTime() {
        return time;
    }

    public String getAvatarResId() {
        return avatarResId;
    }
    // Setters (如果需要的话)
    // ...
}