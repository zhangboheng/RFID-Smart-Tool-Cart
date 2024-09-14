package com.example.keyadministrator;

public class User {
    private int id;
    private String username;
    private String password;
    private String userPart;
    private String userPhone;
    private String faceId;
    private String imagePath;
    private String cardId;
    private int ck0;
    private int ck1;
    private int ck2;
    private int ck3;
    private int ck4;
    private int ck5;
    private String startTime;
    private String endTime;
    private int enabled;

    // 构造函数
    public User(int id, String username, String password, String userPart, String userPhone, String faceId, String imagePath, String cardId, int ck0, int ck1, int ck2, int ck3, int ck4, int ck5, String startTime, String endTime, int enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userPart = userPart;
        this.userPhone = userPhone;
        this.faceId = faceId;
        this.imagePath = imagePath;
        this.cardId = cardId;
        this.ck0 = ck0;
        this.ck1 = ck1;
        this.ck2 = ck2;
        this.ck3 = ck3;
        this.ck4 = ck4;
        this.ck5 = ck5;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }
    // 重写构造函数
    public User(String username, String password, String userPart, String userPhone, String cardId, int ck0, int ck1, int ck2, int ck3, int ck4, int ck5, String startTime, String endTime, int enabled) {
        this.username = username;
        this.password = password;
        this.userPart = userPart;
        this.userPhone = userPhone;
        this.cardId = cardId;
        this.ck0 = ck0;
        this.ck1 = ck1;
        this.ck2 = ck2;
        this.ck3 = ck3;
        this.ck4 = ck4;
        this.ck5 = ck5;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserPart() {
        return userPart;
    }

    public void setUserPart(String userPart) {
        this.userPart = userPart;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCardId() {return cardId;}

    public void setCardId() {this.cardId = cardId;}

    public int isCk0() {
        return ck0;
    }

    public void setCk0(int ck0) {
        this.ck0 = ck0;
    }

    public int isCk1() {return ck1;
    }

    public void setCk1(int ck1) {
        this.ck1 = ck1;
    }

    public int isCk2() {
        return ck2;
    }

    public void setCk2(int ck2) {
        this.ck2 = ck2;
    }

    public int isCk3() {
        return ck3;
    }

    public void setCk3(int ck3) {
        this.ck3 = ck3;
    }

    public int isCk4() {
        return ck4;
    }

    public void setCk4(int ck4) {
        this.ck4 = ck4;
    }

    public int isCk5() {
        return ck5;
    }

    public void setCk5(int ck5) {
        this.ck5 = ck5;
    }

    public String getStartTime() { return startTime; }

    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }

    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getEnabled() { return enabled; }

    public void setEnabled(int enabled) { this.enabled = enabled; }
}