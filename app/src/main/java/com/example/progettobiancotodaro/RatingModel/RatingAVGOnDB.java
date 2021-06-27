package com.example.progettobiancotodaro.RatingModel;


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

    @Override
    public String toString() {
        return ""+ratingAVG;
    }
}
