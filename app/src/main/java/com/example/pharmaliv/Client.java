package com.example.pharmaliv;

public class Client extends Person {

    private String login_ID;

    public Client(String loginID, String firstName, String familyName, String phone) {
        super(firstName, familyName, phone);
        this.login_ID = loginID;
    }

    public Client() {
        super();
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
}
