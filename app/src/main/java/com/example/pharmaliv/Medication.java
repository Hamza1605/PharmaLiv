package com.example.pharmaliv;

class Medication {

    private String med_id;
    private String name;
    private int quantity;

    Medication(String med_id, String name, int quantity) {
        this.med_id = med_id;
        this.name = name;
        this.quantity = quantity;
    }

    Medication(String med_id, String name) {
        this.med_id = med_id;
        this.name = name;
    }

    Medication(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getMed_id() {
        return med_id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
