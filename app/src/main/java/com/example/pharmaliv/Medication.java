package com.example.pharmaliv;

class Medication {
    String med_id;
    String name;
    String quantity;

    Medication(String med_id, String name, String quantity) {
        this.med_id = med_id;
        this.name = name;
        this.quantity = quantity;
    }

    Medication(String med_id, String name) {
        this.med_id = med_id;
        this.name = name;
    }

}
