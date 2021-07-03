package it.bestclient.android.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingLocal;

import java.text.ParseException;
import java.util.Date;

public class DBhelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DBNAME="RATINGS";
    public static final String TABLE_NAME="RATINGS";

    public static final String COL_ID="_id";
    public static final String COL_RATING="rating";
    public static final String COL_CELL="number";
    public static final String COL_DATE="data";
    public static final String COL_COMMENT="comment";
    public static final String COL_FIREBASE_KEY="firebase_key";

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
                COL_RATING+" REAL ,"+
                COL_FIREBASE_KEY+" TEXT " +
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


/* will be called only in case where user updates their app with newer version */
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
    public boolean addData(RatingLocal r) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CELL, r.getNumero());
        contentValues.put(COL_DATE, r.getDate());
        contentValues.put(COL_RATING, r.getVoto());
        contentValues.put(COL_COMMENT, r.getCommento());
        contentValues.put(COL_FIREBASE_KEY, r.get_firebase_key());

        Log.d(TAG, "addData: " +
                "Adding Number: " + r.getNumero() +
                " and Data: " +r.getDate()+
                " and RATING: " +r.getVoto()+
                " and COMMENT: " +r.getCommento()+
                " and FIREBASE KEY: " +r.get_firebase_key()+
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

    public int getRatingId(RatingLocal r) throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME+ " WHERE "+COL_CELL+" = "+r.getNumero();
        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()){
            Date d2= Rating.formatter.parse(c.getString(2));
            boolean same = r.group_by(new RatingLocal(r.getNumero(),d2));
            if (d2 == null) {
                return -1;
            }
            if(same){
                int val = c.getInt(0);
                c.close();
                return val;
            }
        }
        c.close();
        return -1;
    }
/*
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
*/
    public int updateRating(RatingLocal r) throws ParseException  {
        int id = getRatingId(r);
        if(id == -1){
            Log.d(TAG, " CAN'T FIND ID");
            return -1;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME
                + " SET " + COL_RATING + " = "+r.getVoto() + ", "
                + COL_COMMENT +" = '"+r.getCommento() + "', "
                + COL_FIREBASE_KEY +" = '"+r.get_firebase_key()+ "'"
                +" WHERE "+COL_ID+" = "+id;
        Log.d(TAG, "updateRating: query: " + query);
        Log.d(TAG, "updateRating: Setting rate to " + r.getVoto());
        db.execSQL(query);
        return 0;
    }
}