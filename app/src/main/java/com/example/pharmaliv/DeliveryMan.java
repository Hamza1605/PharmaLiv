package com.example.pharmaliv;

public class DeliveryMan extends Person {

    private String login_ID;
    private String state;

    public DeliveryMan(String loginID, String firstName, String familyName, String phone, String state) {
        super(firstName, familyName, phone);
        this.login_ID = loginID;
        this.state = state;
    }

    public DeliveryMan() {
    }

    public String getFamily_Name() {
        return super.getFamily_Name();
    }

    public String getFirst_Name() {
        return super.getFirst_Name();
    }

    public String getPhone() {
        return super.getPhone();
    }

    public String getLogin_ID() {
        return login_ID;
    }

    public String getState() {
        return state;
    }
}
