package com.example.keyadministrator;

public class ValidationResult {
    private final boolean isValid;
    private final int enabled;

    public ValidationResult(boolean isValid, int enabled) {this.isValid = isValid;
        this.enabled = enabled;
    }

    // 添加 getter 方法
    public boolean isValid() {
        return isValid;
    }

    public int getEnabled() {
        return enabled;
    }
}
