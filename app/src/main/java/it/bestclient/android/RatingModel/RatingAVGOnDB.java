package it.bestclient.android.RatingModel;


public class RatingAVGOnDB{
    private String commentList;
    private double votoMedio;
    private int nValutazioni;


    public RatingAVGOnDB(String commentList, double votoMedio, int nValutazioni){
        this.commentList = commentList;
        this.votoMedio = votoMedio;
        this.nValutazioni = nValutazioni;
    }

    public RatingAVGOnDB(){

    }


    public double getVotoMedio(){
        return this.votoMedio;
    }
    public String getCommentList(){
        return this.commentList;
    }
    public int getnValutazioni(){
        return this.nValutazioni;
    }


    public void setVotoMedio(double votoMedio){
        this.votoMedio = votoMedio;
    }
    public void setCommentList(String commentList){
        this.commentList = commentList;
    }
    public void setnValutazioni(int nValutazioni){
        this.nValutazioni = nValutazioni;
    }

    @Override
    public String toString() {
        return ""+votoMedio+", commentList: "+commentList+", nValutazioni: "+nValutazioni;
    }
}
