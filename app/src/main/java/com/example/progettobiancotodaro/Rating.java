package com.example.progettobiancotodaro;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rating {
    //private static AtomicInteger ID_GENERATOR = new AtomicInteger(1000);
   // private int id;
    private String phoneNumber;
    private Date date;
    private String comment = "";
    private float rating = -1;
    public final int DAYS_TO_GROUP_BY = 1;

    public Rating(String phoneNumber, Date date, float rating, String comment){
        this.phoneNumber = phoneNumber.length() <= 10 ? phoneNumber :  phoneNumber.substring(phoneNumber.length()-10);
        this.date = date;
        this.rating = rating;
        this.comment = comment;
       // this.id = ID_GENERATOR.getAndIncrement();
    }

    public Rating(String phoneNumber, Date date){
        this.phoneNumber = phoneNumber.length() <= 10 ? phoneNumber :  phoneNumber.substring(phoneNumber.length()-10);
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

    public Date getRealDate(){
        return date;
    }

    public String getYear(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.ITALIAN);

        return format.format(date);
    }

    public String getMonth(){
        SimpleDateFormat format = new SimpleDateFormat("M", Locale.ITALIAN);

        return format.format(date);
    }

    public String getDay(){
        SimpleDateFormat format = new SimpleDateFormat("dd", Locale.ITALIAN);

        return format.format(date);
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating){
        this.rating = rating;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    @NonNull
    public String toString(){
        String ratingString =  rating == -1 ? "-" : String.valueOf(rating);
        return "Number: "+phoneNumber+", Date: "+getDate()+", Current Rating: "+ ratingString+", Comment: "+comment;
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
        return getYear().equals(rating.getYear()) && getMonth().equals(rating.getMonth()) && getDay().equals(rating.getDay());
        /*long diffInMillies = Math.abs(date.getTime() - rating.date.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diff < DAYS_TO_GROUP_BY;*/
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
