package com.example.progettobiancotodaro.RatingModel;

import java.util.Date;

public class RatingLocal extends Rating{
    /* THIS CLASS REPRESENT RATINGS IN LOCAL DB SQLite AND in the APP */

    public RatingLocal(String numero, Date date, float voto, String commento) {
        super(numero, date, voto, commento);
    }

    public RatingLocal(String numero, Date date) {
        super(numero, date);
    }
}
