package com.example.progettobiancotodaro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        SimpleDateFormat format = new SimpleDateFormat("dd/M/yyyy HH:mm", Locale.ITALIAN);

        return format.format(date);
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating){
        this.rating = rating;
    }

    public String toString(){
        return "Number: "+phoneNumber+", Date: "+getDate()+", Current Rating: "+rating;
    }
}
