package it.bestclient.android.RatingModel;

import java.util.Date;

public class RatingCallLog {
    String numero;
    String date;

    public RatingCallLog(String numero, Date date){
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.date = Rating.formatter.format(date);
    }

    public String getNumero() {
        return numero;
    }

    public String getDate() {
        return date;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setDate(String date) {
        this.date = date;
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

    public boolean group_by(Object o)  {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RatingCallLog rating = (RatingCallLog) o;

        return numero.equals(rating.numero);
        //RAGGRUPPO PER NUMERO DI TELEFONO
    }
}
