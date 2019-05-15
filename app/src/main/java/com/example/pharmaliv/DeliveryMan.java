package com.example.pharmaliv;

public class DeliveryMan extends Person {

    private String loginID;
    private String state;

    public DeliveryMan(String familyName, String firstName, String phone, String loginID, String state) {
        super(familyName, firstName, phone);
        this.loginID = loginID;
        this.state = state;
    }

    public String getFamilyName() {
        return super.getFamilyName();
    }

    public String getFirstName() {
        return super.getFirstName();
    }

    public String getPhone() {
        return super.getPhone();
    }

    public String getLoginID() {
        return loginID;
    }

    public String getState() {
        return state;
    }
}
