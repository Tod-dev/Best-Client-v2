package com.example.progettobiancotodaro;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Rating {
    private String phoneNumber;
    private Date date;
    private float rating;

    public Rating(String phoneNumber, Date date, float rating){
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.rating = rating;
    }

    public Rating(String phoneNumber, Date date){
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.rating = 0;
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

    @NonNull
    public String toString(){
        return "Number: "+phoneNumber+", Date: "+getDate()+", Current Rating: "+rating;
    }

    public boolean equals(Object o, int days) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;

        if (!phoneNumber.equals(rating.phoneNumber)) return false;
        long diffInMillies = Math.abs(date.getTime() - rating.date.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff < days;
    }

}
