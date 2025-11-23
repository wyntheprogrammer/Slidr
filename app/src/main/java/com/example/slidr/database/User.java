package com.example.slidr.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String username;
    private String email;
    private String password; // In production, this should be hashed
    private String avatarUrl;
    private long createdAt;
    private long lastLoginAt;
    private boolean isLoggedIn;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatarUrl = "default";
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
        this.isLoggedIn = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
}