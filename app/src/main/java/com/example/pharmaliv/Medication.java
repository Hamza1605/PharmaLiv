package com.example.pharmaliv;

class Medication {

    private String ID;
    private String Name;
    private int Quantity;

    Medication(String med_id, String name, int quantity) {
        this.ID = med_id;
        this.Name = name;
        this.Quantity = quantity;
    }

    Medication(String med_id, String name) {
        this.ID = med_id;
        this.Name = name;
    }

    Medication(String name, int quantity) {
        this.Name = name;
        this.Quantity = quantity;
    }

    Medication() {
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return Name;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int Quantity) {
        this.Quantity = Quantity;
    }
}
