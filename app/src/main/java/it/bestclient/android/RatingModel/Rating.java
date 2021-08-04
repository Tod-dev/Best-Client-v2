package it.bestclient.android.RatingModel;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rating implements Voto{

    //ATTRIBUTES common to ratingLocal and RatingBIGONDB
    private String numero;
    private long date;
    private String commento;//optional
    private String nome;//optional
    private double voto;
    private double voto_medio;
    private boolean pubblica;
    private int nValutazioni;
    private String commentList;
    // double voto;
    //private long id;

    //utils -> per comodità di utilizzo ci salviamo la data come stringa formattata -> coerente con la stringa in SQLlite
    //uso lo stesso format in tutta l'app!
    public static final SimpleDateFormat formatter  = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN); //formatter.format(date);

    public Rating(String numero, Date date, double voto, String commento, double voto_medio, String nome, boolean pubblica, int nValutazioni){
        this.voto = voto;
        //rating con voto e commento, data sotto forma di date
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.commento = commento;
        this.date = date.getTime();
        this.nome = nome;
        this.pubblica = pubblica;
        this.voto_medio = voto_medio;
        this.nValutazioni = nValutazioni;
        // this.id = ID_GENERATOR.getAndIncrement();
    }

    public Rating(){
        this.numero = "";
        this.voto = -1;
        this.commento ="";
        this.date = new Date().getTime();
        this.nome = "";
        this.pubblica = false;
        this.commentList = "";
        this.voto_medio = -1;
        this.nValutazioni = 0;
    }

    public Rating(String numero){
        //nuovo rating senza voto e senza commento
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.voto = -1;
        this.commento ="";
        this.date = new Date().getTime();
        this.nome = "";
        this.pubblica = false;
        this.commentList = "";
        this.voto_medio = -1;
        this.nValutazioni = 0;
        //  this.id = ID_GENERATOR.getAndIncrement();
    }

    public Rating(String numero, String nome){
        //nuovo rating senza voto e senza commento
        this.numero = numero.length() <= 10 ? numero :  numero.substring(numero.length()-10);
        this.voto = -1;
        this.commento ="";
        this.date = new Date().getTime();
        this.nome = nome;
        this.pubblica = false;
        this.commentList = "";
        this.voto_medio = -1;
        this.nValutazioni = 0;
        //  this.id = ID_GENERATOR.getAndIncrement();
    }

    public double getVoto() {
        return voto;
    }

    public boolean getPubblica() {
        return pubblica;
    }

    public String getCommentList() {
        return commentList;
    }

    public String getNumero() {
        return numero;
    }

    public long getDate(){
        return this.date;
    }

    public String getNome(){
        return this.nome;
    }

    public String getCommento(){
        return commento;
    }

    public double getVoto_medio(){
        return voto_medio;
    }

    public int getnValutazioni(){
        return this.nValutazioni;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPubblica(boolean pubblica) {
        this.pubblica = pubblica;
    }

    public void setVoto_medio(double voto_medio) {
        this.voto_medio = voto_medio;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setVoto(double voto) {
        this.voto = voto;
    }

    public void setCommentList(String commentList) {
        this.commentList = commentList;
    }

    public void setCommento(String commento){
        this.commento = commento;
    }

    public void setnValutazioni(int nValutazioni){
        this.nValutazioni = nValutazioni;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "numero='" + numero + '\'' +
                ", date='" + date + '\'' +
                ", commento='" + commento + '\'' +
                ", voto=" + voto +
                ", voto_medio=" + voto_medio +
                ", nome='" + nome +
                "', pubblica=" + pubblica +
                "', nValutazioni=" + nValutazioni +
                '}';
    }
}
