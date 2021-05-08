package com.example.progettobiancotodaro;

public class RatingAVGOnDB {
    private double ratingAVG;


    public RatingAVGOnDB(double ratingAVG){
        this.ratingAVG = ratingAVG;
    }


    public double getRating(){
        return this.ratingAVG;
    }


    public void setRating(double ratingAVG){
        this.ratingAVG = ratingAVG;
    }

}
