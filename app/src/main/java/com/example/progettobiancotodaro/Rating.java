package com.example.progettobiancotodaro;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Rating {
    private static AtomicInteger ID_GENERATOR = new AtomicInteger(1000);
   // private int id;
    private String phoneNumber;
    private Date date;
    private float rating = -1;
    public final int DAYS_TO_GROUP_BY = 1;

    public Rating(String phoneNumber, Date date, float rating){
        this.phoneNumber = phoneNumber.substring(phoneNumber.length()-10);
        this.date = date;
        this.rating = rating;
       // this.id = ID_GENERATOR.getAndIncrement();
    }

    public Rating(String phoneNumber, Date date){
        this.phoneNumber = phoneNumber.substring(phoneNumber.length()-10);
        this.date = date;
      //  this.id = ID_GENERATOR.getAndIncrement();
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
        String ratingString =  rating == -1 ? "-" : String.valueOf(rating);
        return "Number: "+phoneNumber+", Date: "+getDate()+", Current Rating: "+ ratingString;
    }

    /*
    public String getStringInstance(){
        String ratingString =  rating == -1 ? "-" : String.valueOf(rating);
        return "{id:"+ id +", Number: "+phoneNumber+", Date: "+getDate()+", Current Rating: "+ ratingString+"}";
    }
    */
    public boolean group_by(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;

        if (!phoneNumber.equals(rating.phoneNumber)) return false;
        long diffInMillies = Math.abs(date.getTime() - rating.date.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff < DAYS_TO_GROUP_BY;
    }

    public boolean group_by_number(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;

        return phoneNumber.equals(rating.phoneNumber);
    }
    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;

        return id == rating.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
    */
}
