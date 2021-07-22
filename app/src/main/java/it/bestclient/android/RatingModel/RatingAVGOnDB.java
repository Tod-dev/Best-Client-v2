package it.bestclient.android.RatingModel;


public class RatingAVGOnDB{
    private String commentList;
    private double votoMedio;


    public RatingAVGOnDB(String commentList, double votoMedio){
        this.commentList = commentList;
        this.votoMedio = votoMedio;
    }

    public RatingAVGOnDB(){

    }


    public double getVotoMedio(){
        return this.votoMedio;
    }
    public String getCommentList(){
        return this.commentList;
    }


    public void setVotoMedio(double votoMedio){
        this.votoMedio = votoMedio;
    }
    public void setCommentList(String commentList){
        this.commentList = commentList;
    }

    @Override
    public String toString() {
        return ""+votoMedio+", commentList: "+commentList;
    }
}
