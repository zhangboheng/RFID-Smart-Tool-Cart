package com.example.keyadministrator;

public class Good {
    private int id;
    private String rfid;
    private String typeName;
    private String manufacturer;
    private int lifetime;
    private String borrowStartTime;
    private String returnDueTime;
    private int borrowDuration;
    private int cabinetNumberChecked;
    private String belongingCabinetNumber;
    private String actualBelongingCabinetNumber;
    private int borrowOrNOt; // 1借走，0还在

    // 构造函数
    public Good(int id, String rfid, String typeName, String manufacturer, int lifetime,
                String borrowStartTime, String returnDueTime, int borrowDuration,
                int cabinetNumberChecked, String belongingCabinetNumber, String actualBelongingCabinetNumber, int borrowOrNOt) {
        this.id = id;
        this.rfid = rfid;
        this.typeName = typeName;
        this.manufacturer = manufacturer;
        this.lifetime = lifetime;
        this.borrowStartTime = borrowStartTime;
        this.returnDueTime = returnDueTime;
        this.borrowDuration = borrowDuration;
        this.cabinetNumberChecked = cabinetNumberChecked;
        this.belongingCabinetNumber = belongingCabinetNumber;
        this.actualBelongingCabinetNumber = actualBelongingCabinetNumber;
        this.borrowOrNOt = borrowOrNOt;
    }
    // 重新构造函数
    public Good(String rfid, String typeName, String manufacturer, int lifetime,
                String borrowStartTime, String returnDueTime, int borrowDuration,
                int cabinetNumberChecked, String belongingCabinetNumber, String actualBelongingCabinetNumber, int borrowOrNOt) {
        this.rfid = rfid;
        this.typeName = typeName;
        this.manufacturer = manufacturer;
        this.lifetime = lifetime;
        this.borrowStartTime = borrowStartTime;
        this.returnDueTime = returnDueTime;
        this.borrowDuration = borrowDuration;
        this.cabinetNumberChecked = cabinetNumberChecked;
        this.belongingCabinetNumber = belongingCabinetNumber;
        this.actualBelongingCabinetNumber = actualBelongingCabinetNumber;
        this.borrowOrNOt = borrowOrNOt;
    }
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {this.lifetime = lifetime;
    }

    public String getBorrowStartTime() {
        return borrowStartTime;
    }

    public void setBorrowStartTime(String borrowStartTime) {
        this.borrowStartTime = borrowStartTime;
    }

    public String getReturnDueTime() {
        return returnDueTime;
    }

    public void setReturnDueTime(String returnDueTime) {
        this.returnDueTime = returnDueTime;
    }

    public int getBorrowDuration() {
        return borrowDuration;
    }

    public void setBorrowDuration(int borrowDuration) {
        this.borrowDuration = borrowDuration;
    }

    public int getCabinetNumberChecked() {
        return cabinetNumberChecked;
    }

    public void setCabinetNumberChecked(int cabinetNumberChecked) {
        this.cabinetNumberChecked = cabinetNumberChecked;
    }

    public String getBelongingCabinetNumber() {
        return belongingCabinetNumber;
    }

    public void setBelongingCabinetNumber(String belongingCabinetNumber) {
        this.belongingCabinetNumber = belongingCabinetNumber;
    }

    public String getActualBelongingCabinetNumber() {
        return actualBelongingCabinetNumber;
    }

    public void setActualBelongingCabinetNumber(String actualBelongingCabinetNumber) {
        this.actualBelongingCabinetNumber = actualBelongingCabinetNumber;
    }

    public int getBorrowOrNOt() {return borrowOrNOt; }

    public void setBorrowOrNOt(int borrowOrNOt) {
        this.borrowOrNOt = borrowOrNOt;
    }
}
