package com.example.progettobiancotodaro.RatingModel;

import java.util.Date;

public class RatingLocal extends Rating{
    /* THIS CLASS REPRESENT RATINGS IN LOCAL DB SQLite AND in the APP */

    //CONSTANTS
    public final int DAYS_TO_GROUP_BY = 1; // group by every rating by number and day -> how many days?

    public RatingLocal(String numero, Date date, float voto, String commento) {
        super(numero, date, voto, commento);
    }

    public RatingLocal(String numero, Date date) {
        super(numero, date);
    }
}
