package com.kot.model;

public class User {
    private int userId;
    private String name;
    private String username;
    private String role;
    private boolean isFirstLogin;

    public User(int userId, String name, String username, String role, boolean isFirstLogin) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.role = role;
        this.isFirstLogin = isFirstLogin;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }
}
