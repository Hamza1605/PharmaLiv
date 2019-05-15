package com.example.pharmaliv;

public class Client extends Person {

    private String loginID;

    public Client(String familyName, String firstName, String phone, String loginID) {
        super(familyName, firstName, phone);
        this.loginID = loginID;
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
}
