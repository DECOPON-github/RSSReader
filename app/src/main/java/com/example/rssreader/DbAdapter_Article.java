package com.example.rssreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter_Article {
    static final String DATABASE_NAME = "article.db";
    static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_ARTICLE = "article";
    public static final String COL_ID = "_id";
    public static final String COL_ID_SITE = "id_site";
    public static final String COL_TITLE = "title";
    public static final String COL_DATE = "date";
    public static final String COL_URL = "url";
    public static final String COL_READ = "read";

    public static final String TABLE_NAME_SITE = "site";
    public static final String COL_SITE = "site";
    public static final String COL_BAN = "ban";

    protected final Context context;
    protected DbAdapter_Article.DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DbAdapter_Article(Context context){
        this.context = context;
        dbHelper = new DbAdapter_Article.DatabaseHelper(this.context);
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
                    "CREATE TABLE " + TABLE_NAME_SITE + " ("
                            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COL_SITE + " TEXT NOT NULL,"
                            + COL_BAN + " INTEGER DEFAULT 0);");

            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME_ARTICLE + " ("
                            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COL_ID_SITE + " INTEGER NOT NULL,"
                            + COL_TITLE + " TEXT NOT NULL,"
                            + COL_DATE + " TEXT NOT NULL,"
                            + COL_URL + " TEXT UNIQUE NOT NULL,"
                            + COL_READ + " INTEGER DEFAULT 0);");
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SITE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ARTICLE);
            onCreate(db);
        }
    }

    //
    // Adapter Methods
    //
    public DbAdapter_Article open() {
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
}
