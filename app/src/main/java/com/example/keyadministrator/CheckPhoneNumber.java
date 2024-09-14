package com.example.keyadministrator;

public class CheckPhoneNumber {
    public static boolean isPhone(String Phone_number) {
        return Phone_number.matches("^1[34578]\\d{9}$");
    }
}
