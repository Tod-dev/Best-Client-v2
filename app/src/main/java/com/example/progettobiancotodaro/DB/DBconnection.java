package com.example.progettobiancotodaro.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBconnection extends SQLiteOpenHelper {
    public static final String DBNAME="RATINGS";
    public DBconnection(Context context) {
        super(context, DBNAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="CREATE TABLE "+DatabaseStrings.TBL_RATINGS+
                " ( "+DatabaseStrings.FIELD_RATING+"INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseStrings.FIELD_RATING+" TEXT," +
                DatabaseStrings.FIELD_TEXT+" TEXT," +
                DatabaseStrings.FIELD_DATE+" TEXT)";
        db.execSQL(q);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String q = "DROP IF TABLE EXISTS";
        db.execSQL(q+DatabaseStrings.TBL_RATINGS);
        onCreate(db);
    }
}