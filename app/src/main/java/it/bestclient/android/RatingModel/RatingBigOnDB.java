package it.bestclient.android.RatingModel;

import java.util.Date;

public class RatingBigOnDB implements Voto{

    //attriubutes
    private long date;
    private String commento;//optional
    private double voto;
    private boolean pubblico;

    public RatingBigOnDB(long date, String commento, double voto) {
        this.date = date;
        this.commento = commento;
        this.voto = voto;
        this.pubblico = false;
    }

    public RatingBigOnDB(){
        this.pubblico = false;
    }

    public void setPubblica(boolean pubblico){
        this.pubblico = pubblico;
    }

    @Override
    public void setVoto(double voto) {
        this.voto = voto;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    @Override
    public double getVoto() {
        return voto;
    }

    public long getDate() {
        return date;
    }

    public String getCommento() {
        return commento;
    }

    public boolean isPubblica() {
        return pubblico;
    }

}
