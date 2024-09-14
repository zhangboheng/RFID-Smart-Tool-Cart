package com.example.keyadministrator;

// Config 类用于表示参数配置数据
public class Config {
    public int id;
    public String name;
    public String serialPort;
    public int baudRate;

    public Config(int id, String name, String serialPort, int baudRate) {
        this.id = id;
        this.name = name;
        this.serialPort = serialPort;
        this.baudRate = baudRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;}

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
}
