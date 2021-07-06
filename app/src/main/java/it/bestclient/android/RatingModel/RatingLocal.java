package it.bestclient.android.RatingModel;

import java.util.Date;

public class RatingLocal extends Rating{
    /* THIS CLASS REPRESENT RATINGS IN LOCAL DB SQLite AND in the APP */

    private String firebase_key;

    public RatingLocal(String numero, Date date, double voto, String commento,String firebase_key) {
        super(numero, date, voto, commento);
        this.firebase_key = firebase_key;
    }

    public void set_firebase_key(String firebase_key) {
        this.firebase_key = firebase_key;
    }

    public String get_firebase_key() {
        return firebase_key;
    }

    public RatingLocal(String numero, Date date) {
        super(numero, date);
        this.firebase_key = "";
    }

    public String toString(){
        return super.toString()+", Key: "+this.firebase_key;
    }
}
