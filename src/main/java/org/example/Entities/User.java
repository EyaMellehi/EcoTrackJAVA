package org.example.Entities;

public class User {
    private int id;
    private String email;
    private String roles;
    private String password;
    private String name;
    private String phone;
    private String region;
    private int points;
    private boolean isActive;
    private String image;
    private String delegation;
    private String faceioId;

    private boolean twoFactorEnabled;
    private String twoFactorSecret;

    public User() {
    }

    public User(String email, String roles, String password, String name, String phone, String region) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.region = region;
    }

    public User(int id, String email, String roles, String password, String name, String phone, String region,
                int points, boolean isActive, String image, String delegation, String faceioId,
                boolean twoFactorEnabled, String twoFactorSecret) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.region = region;
        this.points = points;
        this.isActive = isActive;
        this.image = image;
        this.delegation = delegation;
        this.faceioId = faceioId;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorSecret = twoFactorSecret;
    }

    public User(String email, String password, String name, String phone, String region) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.region = region;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDelegation() { return delegation; }
    public void setDelegation(String delegation) { this.delegation = delegation; }

    public String getFaceioId() { return faceioId; }
    public void setFaceioId(String faceioId) { this.faceioId = faceioId; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }
}