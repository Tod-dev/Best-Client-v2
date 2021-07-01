package it.bestclient.android.DB;

import android.annotation.SuppressLint;


import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateOnDB {

    private long  timestamp; // N.B : il timestamp rimane quello +0.00 MA non importa perchè calcoliamo la differenza in minuti dall'ultimo timestamp
    private String formattedData;//formattedData è la stringa che indica la data con il fusorario di Roma
    final long hr2 = 7200000;//2 ore in ms -> differenza tra + 0.00 e +2.00

    public DateOnDB(long timestamp, String formattedData) {
        this.timestamp = timestamp;
        this.formattedData = formattedData;
    }

    public DateOnDB() {
    }

    public DateOnDB(long timestamp) {
        this.timestamp = timestamp;
        this.formattedData = format().format(timestamp+hr2); //salvo ila stringa con il fusorario di Roma
    }

    public DateOnDB(String formattedData) throws ParseException {
        this.timestamp = format().parse(formattedData).getTime();
        this.formattedData = formattedData;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedData() {
        return formattedData;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "DateOnDB{" +
                "timestamp=" + timestamp +
                ", formattedData='" + formattedData + '\'' +
                '}';
    }

    @SuppressLint("SimpleDateFormat")
    static SimpleDateFormat format (){
        return  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }
}
