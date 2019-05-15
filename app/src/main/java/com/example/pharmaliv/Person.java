package com.example.pharmaliv;

public class Person {
    private String familyName;
    private String firstName;
    private String phone;

    public Person(String familyName, String firstName, String phone) {
        this.familyName = familyName;
        this.firstName = firstName;
        this.phone = phone;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPhone() {
        return phone;
    }
}
