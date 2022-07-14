package com.example.rssreader.DbAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter_Favorite {
    static final String DATABASE_NAME = "favorite.db";
    static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "favorite";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DATE = "date";
    public static final String COL_URL = "url";
    public static final String COL_SITE = "site";

    protected final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DbAdapter_Favorite(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }

    //
    // SQLiteOpenHelper
    //
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME + " ("
                            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COL_TITLE + " TEXT NOT NULL,"
                            + COL_DATE + " TEXT NOT NULL,"
                            + COL_URL + " TEXT NOT NULL,"
                            + COL_SITE + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    //
    // Adapter Methods
    //
    public DbAdapter_Favorite open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void begin() {
        if (db != null) {
            db.beginTransaction();
        }
    }

    public void success () {
        if (db != null) {
            db.setTransactionSuccessful();
        }
    }

    public void end() {
        if (db != null) {
            db.endTransaction();
        }
    }

    public void close(){
        dbHelper.close();
    }

    //
    // App Methods
    //
    public void  deleteTable(){
        db.execSQL("delete from " + TABLE_NAME);
        db.execSQL("delete from sqlite_sequence where name=\'" + TABLE_NAME + "\'");
    }

    public void  deleteRecordByUrl (String url) {
        db.execSQL("delete from " + TABLE_NAME + " where " + COL_URL + " = " + "\"" + url + "\"");
    }

    public Cursor getTable(){
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public void saveItem(String title,String date, String url, String site){
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DATE, date);
        values.put(COL_URL, url);
        values.put(COL_SITE, site);
        db.insertOrThrow(TABLE_NAME, null, values);
    }

    public boolean isUrl (String url) {
        boolean flag = true;

        Cursor c = db.query(TABLE_NAME, null, COL_URL +" = ?", new String[]{url},null, null, null);
        if (c.getCount() == 0) {
            flag = false;
        }

        c.close();
        return flag;
    }
}
