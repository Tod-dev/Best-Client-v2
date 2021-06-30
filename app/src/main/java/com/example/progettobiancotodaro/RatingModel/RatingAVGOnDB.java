package com.example.progettobiancotodaro.RatingModel;


public class RatingAVGOnDB implements Voto{
    private double ratingAVG;


    public RatingAVGOnDB(double ratingAVG){
        this.ratingAVG = ratingAVG;
    }


    public double getVoto(){
        return this.ratingAVG;
    }


    public void setVoto(double voto){
        this.ratingAVG = voto;
    }

    @Override
    public String toString() {
        return ""+ratingAVG;
    }
}
