package com.example.pharmaliv;

public class Client {

    public String Family_Name;
    public String First_Name;
    public String Login_ID;
    public String Phone;

    public Client(){
    }

    public Client(String Family_Name, String First_Name, String Login_ID, String Phone){
        this.Family_Name = Family_Name;
        this.First_Name = First_Name;
        this.Login_ID= Login_ID;
        this.Phone = Phone;
    }
}
