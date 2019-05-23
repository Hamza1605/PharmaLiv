package com.example.pharmaliv;

public class Person {

    private String first_Name;
    private String family_Name;
    private String phone;

    public Person(String firstName, String familyName, String phone) {
        this.family_Name = familyName;
        this.first_Name = firstName;
        this.phone = phone;
    }

    public Person() {
    }

    public String getFamily_Name() {
        return family_Name;
    }

    public String getFirst_Name() {
        return first_Name;
    }

    public String getPhone() {
        return phone;
    }
}
