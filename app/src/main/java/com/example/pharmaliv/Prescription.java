package com.example.pharmaliv;

public class Prescription {

    private String id;
    private String client_ID;
    private String pharmacy_ID;
    private String delivery_ID;
    private String state;
    private String sending_Date;
    private String sending_Time;
    private String delivery_Date;
    private String delivery_Time;
    private String latitude;
    private String longitude;
    private String client_Note;
    private String deliveryMan_Note;
    private String total;

    public Prescription(String ID, String Client_ID, String Pharmacy_ID, String State, String Sending_Date,
                        String Sending_Time, String Delivery_Date, String Delivery_Time,
                        String Latitude, String Longitude, String Client_Note, String DeliveryMan_Note, String Total) {
        this.id = ID;
        this.client_ID = Client_ID;
        this.pharmacy_ID = Pharmacy_ID;

        this.state = State;
        this.sending_Time = Sending_Time;
        this.sending_Date = Sending_Date;
        this.delivery_Date = Delivery_Date;
        this.delivery_Time = Delivery_Time;
        this.latitude = Latitude;
        this.longitude = Longitude;
        this.client_Note = Client_Note;
        this.deliveryMan_Note = DeliveryMan_Note;
        this.total = Total;
    }

    public Prescription() {
    }


    public String getClient_ID() {
        return client_ID;
    }

    public void setClient_ID(String client_ID) {
        this.client_ID = client_ID;
    }

    public String getPharmacy_ID() {
        return pharmacy_ID;
    }

    public void setPharmacy_ID(String pharmacy_ID) {
        this.pharmacy_ID = pharmacy_ID;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSending_Date() {
        return sending_Date;
    }

    public void setSending_Date(String sending_Date) {
        this.sending_Date = sending_Date;
    }

    public String getDelivery_Date() {
        return delivery_Date;
    }

    public void setDelivery_Date(String delivery_Date) {
        this.delivery_Date = delivery_Date;
    }

    public String getSending_Time() {
        return sending_Time;
    }

    public void setSending_Time(String sending_Time) {
        this.sending_Time = sending_Time;
    }

    public String getDelivery_Time() {
        return delivery_Time;
    }

    public void setDelivery_Time(String delivery_Time) {
        this.delivery_Time = delivery_Time;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getClient_Note() {
        return client_Note;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setClient_Note(String client_Note) {
        this.client_Note = client_Note;
    }

    public String getDeliveryMan_Note() {
        return deliveryMan_Note;
    }

    public void setDeliveryMan_Note(String deliveryMan_Note) {
        this.deliveryMan_Note = deliveryMan_Note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDelivery_ID() {
        return delivery_ID;
    }

    public void setDelivery_ID(String delivery_ID) {
        this.delivery_ID = delivery_ID;
    }
}
