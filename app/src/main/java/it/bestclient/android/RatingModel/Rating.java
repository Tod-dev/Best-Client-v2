package it.bestclient.android.RatingModel;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rating implements Voto{

    //ATTRIBUTES common to ratingLocal and RatingBIGONDB
    private String numero;
    private String date;
    private String commento;//optional
    private double voto;
    private double voto_medio;
    // double voto;
    //private long id;

    //utils -> per comodità di utilizzo ci salviamo la data come stringa formattata -> coerente con la stringa in SQLlite
    //uso lo stesso format in tutta l'app!
    public static final SimpleDateFormat formatter  = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN); //formatter.format(date);

    public Rating(String numero, Date date, double voto, String commento, double voto_medio){
        this.voto = voto;
        //rating con voto e commento, data sotto forma di date
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        if(date == null)
            this.date = "";
        else
            this.date = formatter.format(date);
        this.commento = commento;
        this.voto_medio = voto_medio;
        // this.id = ID_GENERATOR.getAndIncrement();
    }

    public Rating(){

    }

    public Rating(String numero){
        //nuovo rating senza voto e senza commento
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.voto = -1;
        this.commento ="";
        this.date = "";
        this.voto_medio = -1;
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

    public double getVoto_medio(){
        return voto_medio;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setVoto_medio(double voto_medio) {
        this.voto_medio = voto_medio;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVoto(double voto) {
        this.voto = voto;
    }


    public void setCommento(String commento){
        this.commento = commento;
    }

    @NonNull
    public String toString(){
        String ratingString =   this.voto == -1 ? "-" : String.valueOf(this.voto);
        return "Number: "+ this.numero +", Date: "+this.date+", Current Rating: "+ ratingString+", Comment: "+ this.commento;
    }



}
