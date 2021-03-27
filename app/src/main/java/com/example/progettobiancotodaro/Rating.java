package com.example.progettobiancotodaro;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Rating {
    private String phoneNumber;
    private Date date;
    private float rating;

    public Rating(String phoneNumber, Date date, float rating){
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.rating = rating;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd-mm-yyyy");
        return format.format(date);
    }

    public float getRating() {
        return rating;
    }
}
