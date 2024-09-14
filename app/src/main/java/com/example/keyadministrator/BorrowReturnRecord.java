package com.example.keyadministrator;

public class BorrowReturnRecord {
    private int id;
    private String itemType;
    private String rfid;
    private int userId;
    private String user;
    private String operationType;
    private String cabinetNumber;
    private String operationDate;

    // 构造函数
    public BorrowReturnRecord(int id, String itemType, String rfid, int userId, String user, String operationType, String cabinetNumber, String operationDate) {
        this.id = id;
        this.itemType = itemType;
        this.rfid = rfid;
        this.userId = userId;
        this.user = user;
        this.operationType = operationType;
        this.cabinetNumber = cabinetNumber;
        this.operationDate = operationDate;
    }

    // 构造函数重写
    public BorrowReturnRecord(String itemType, String rfid, int userId, String user, String operationType, String cabinetNumber, String operationDate) {
        this.itemType = itemType;
        this.rfid = rfid;
        this.userId = userId;
        this.user = user;
        this.operationType = operationType;
        this.cabinetNumber = cabinetNumber;
        this.operationDate = operationDate;
    }

    // Getters and Setters
    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getRfid() {
        return rfid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getCabinetNumber() {
        return cabinetNumber;
    }

    public void setCabinetNumber(String cabinetNumber) {
        this.cabinetNumber = cabinetNumber;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        this.operationDate = operationDate;
    }
}
