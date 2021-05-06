package com.example.progettobiancotodaro.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.progettobiancotodaro.Rating;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DBhelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DBNAME="RATINGS";
    public static final String TABLE_NAME="RATINGS";

    public static final String COL_ID="_id";
    public static final String COL_RATING="rating";
    public static final String COL_CELL="number";
    public static final String COL_DATE="data";
    public static final String COL_COMMENT="comment";

    public DBhelper(Context context) {
        super(context, DBNAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {

        String q="CREATE TABLE "+ TABLE_NAME +
                " ( "+
                COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_CELL+" TEXT ," +
                COL_DATE+" TEXT ," +
                COL_COMMENT+" TEXT ,"+
                COL_RATING+" REAL "+
                ")";
        db.execSQL(q);
    }
/*
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        String q = "DROP  TABLE IF EXISTS "+ TABLE_NAME;
        db.execSQL(q);
        onCreate(db);
    }
*/
    /*
    public void addColumn(){
        String q = "alter table "+ TABLE_NAME + " ADD " +COL_COMMENT +" TEXT";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(q);
    }
*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String q = "DROP  TABLE IF EXISTS "+ TABLE_NAME;
        db.execSQL(q);
        onCreate(db);
    }
    /*
    public boolean addData(String cell, String date ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
       // contentValues.put(COL_ID, id);
        contentValues.put(COL_CELL, cell);
        contentValues.put(COL_DATE, date);

        Log.d(TAG, "addData: " +
             //   "Adding _id: " + id +
                "Adding Number: " + cell +
                " and Data: " +date+
                " to: " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        return result != -1;
    }
*/
    public boolean addData(String cell, String date, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
       // contentValues.put(COL_ID, id);
        contentValues.put(COL_CELL, cell);
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_RATING, rating);
        contentValues.put(COL_COMMENT, comment);

        Log.d(TAG, "addData: " +
             //   "Adding _id: " + id +
                "Adding Number: " + cell +
                " and Data: " +date+
                " and RATING: " +rating+
                " to: " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        return result != -1;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public Cursor getData(String cell){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+COL_RATING+" FROM " + TABLE_NAME + " WHERE " + COL_CELL + "=" +cell;
        return db.rawQuery(query, null);
    }

    public int getRatingId(Rating r) throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME+ " WHERE "+COL_CELL+" = "+r.getPhoneNumber();
        Cursor c = db.rawQuery(query, null);
        Date d1 = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(r.getDate());
        while(c.moveToNext()){
            Date d2 =  new SimpleDateFormat("dd/MM/yyyy",Locale.ITALY).parse(c.getString(2));
            long diffInMillies = 0;
            if (d2 != null && d1 != null) {
                diffInMillies = Math.abs(d2.getTime()-d1.getTime());
            }
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if(diff < r.DAYS_TO_GROUP_BY){
                int val = c.getInt(0);
                c.close();
                return val;
            }
        }
        c.close();
        return -1;
    }

    public float getRating(String incomingNumber){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+COL_RATING+" FROM "+TABLE_NAME+" WHERE "+COL_CELL+"="+incomingNumber;
        Cursor c = db.rawQuery(query, null);
        float rating = 0;
        float i = 0;
        while(c.moveToNext()){
            int colRating = c.getColumnIndex(COL_RATING);
            rating += c.getFloat(colRating);
            i++;
        }
        c.close();

        if(i == 0){
            return -1;
        }

        return rating / i;

    }

    public int updateRating(Rating r) throws ParseException {
        int id = getRatingId(r);
        if(id == -1){
            Log.d(TAG, " CAN'T FIND ID");
            return -1;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL_RATING +
                " = "+r.getRating() +" WHERE "+COL_ID+" = "+id;
        Log.d(TAG, "updateRating: query: " + query);
        Log.d(TAG, "updateRating: Setting rate to " + r.getRating());
        db.execSQL(query);
        return 0;
    }
}