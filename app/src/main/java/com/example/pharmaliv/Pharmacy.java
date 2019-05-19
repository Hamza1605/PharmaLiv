package com.example.pharmaliv;

public class Pharmacy {

    private String loginID;
    private String name;
    private Double latitude;
    private Double longitude;
    private String phone;

    public Pharmacy(String loginID, String name, Double latitude, Double longitude, String phone) {
        this.loginID = loginID;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
    }


    public String getLoginID() {
        return loginID;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
