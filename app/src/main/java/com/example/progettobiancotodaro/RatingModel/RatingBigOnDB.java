package com.example.progettobiancotodaro.RatingModel;

public class RatingBigOnDB {
    private String idEsercente;
    private String date;
    private double voto;
    private String numero;
    private String commento; //OPZIONALE

    public RatingBigOnDB(){

    }

    public RatingBigOnDB(String idEsercente, String date, double voto, String numero, String commento) {
        this.idEsercente = idEsercente;
        this.date = date;
        this.voto = voto;
        this.numero = numero;
        this.commento = commento;
    }

    public RatingBigOnDB(String idEsercente, String date, double voto, String numero) { //SENZA COMMENTO
        this.idEsercente = idEsercente;
        this.date = date;
        this.voto = voto;
        this.numero = numero;
        this.commento="";
    }

    public String getIdEsercente() {
        return idEsercente;
    }

    public String getDate() {
        return date;
    }

    public double getVoto() {
        return voto;
    }

    public String getNumero() {
        return numero;
    }

    public String getCommento() {
        return commento;
    }

    public void setIdEsercente(String idEsercente) {
        this.idEsercente = idEsercente;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVoto(double voto) {
        this.voto = voto;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }
}
