package it.bestclient.android.RatingModel;

import java.util.Date;

public class RatingBigOnDB implements Voto{

    //attriubutes
    private String date;
    private String commento;//optional
    private double voto;

    public RatingBigOnDB(String date, String commento, double voto) {
        this.date = date;
        this.commento = commento;
        this.voto = voto;
    }

    @Override
    public void setVoto(double voto) {
        this.voto = voto;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    @Override
    public double getVoto() {
        return voto;
    }

    public String getDate() {
        return date;
    }

    public String getCommento() {
        return commento;
    }
}
