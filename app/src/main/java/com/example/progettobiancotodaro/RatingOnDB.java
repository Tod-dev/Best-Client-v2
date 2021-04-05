package com.example.progettobiancotodaro;

import androidx.annotation.NonNull;

public class RatingOnDB {
    private String phoneNumber;
    private String ratings;

    public RatingOnDB(){

    }

    public RatingOnDB(String phoneNumber, String ratings){
        this.phoneNumber = phoneNumber;
        this.ratings = ratings;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }

    public String getRatings(){
        return this.ratings;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public void setRatings(String ratings){
        this.ratings = ratings;
    }

    @Override
    @NonNull
    public String toString() {
        return "RatingOnDB{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", ratings='" + ratings + '\'' +
                '}';
    }
}
