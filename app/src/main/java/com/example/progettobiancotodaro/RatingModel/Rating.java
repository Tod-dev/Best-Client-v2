package com.example.progettobiancotodaro.RatingModel;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class  Rating implements  Voto{

    //ATTRIBUTES common to ratingLocal and RatingBIGONDB
    private String numero;
    private String date;
    private String commento;//optional
    private double voto;
    // double voto;
    //private long id;

    //utils -> per comodità di utilizzo ci salviamo la data come stringa formattata -> coerente con la stringa in SQLlite
    //uso lo stesso format in tutta l'app!
    public static final SimpleDateFormat formatter  = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALIAN); //formatter.format(date);

    public Rating(String numero, Date date, double voto, String commento){
        this.voto = voto;
        //rating con voto e commento, data sotto forma di date
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.date = formatter.format(date);
        this.commento = commento;
        // this.id = ID_GENERATOR.getAndIncrement();
    }


    public Rating(String numero, Date date){
        this.voto = -1;// not rated
        //nuovo rating senza voto e senza commento
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.date = formatter.format(date);
        this.commento ="";
        //  this.id = ID_GENERATOR.getAndIncrement();
    }

    public double getVoto() {
        return voto;
    }

    public String getNumero() {
        return numero;
    }

    public String getDate(){
        return this.date;
    }


    public String getCommento(){
        return commento;
    }


    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVoto(double voto) {
        this.voto = voto;
    }



    public static String Year(String s) {
        return s.substring(6,10);
    }

    public static String Month(String s){
        return s.substring(3,5);
    }

    public static String Day(String s){
        return s.substring(0,2);
    }


    public void setCommento(String commento){
        this.commento = commento;
    }

    @NonNull
    public String toString(){
        String ratingString =   this.voto == -1 ? "-" : String.valueOf(this.voto);
        return "Number: "+ this.numero +", Date: "+this.date+", Current Rating: "+ ratingString+", Comment: "+ this.commento;
    }

    public boolean group_by(Object o)  {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;
        String date = rating.getDate();

        if (!numero.equals(rating.numero)) return false;
        return Year(this.date).equals(Year(date)) && Month(this.date).equals(Month(date)) && Day(this.date).equals(Day(date));
    }

}
