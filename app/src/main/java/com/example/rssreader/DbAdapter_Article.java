package com.example.rssreader;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.example.rssreader.Constants.FLAG_NO_READ;
import static com.example.rssreader.Constants.FLAG_READ;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    //
    // Article Methods
    //
    public void  deleteTableArticle(){
        db.execSQL("delete from " + TABLE_NAME_ARTICLE);
        db.execSQL("delete from sqlite_sequence where name = \'" + TABLE_NAME_ARTICLE + "\'");
    }

    public void  deleteTableArticle(String beforeTime){
        db.delete(TABLE_NAME_ARTICLE, COL_DATE + " < ?", new String[]{beforeTime});
    }

    public Cursor getTableArticle(){
        return db.query(TABLE_NAME_ARTICLE, null, null, null, null, null, null);
    }

    public Cursor getTableArticle(String word, String orderby){
        return db.query(TABLE_NAME_ARTICLE, null, COL_TITLE + " like ?", new String[]{"%" + word + "%"}, null, null, orderby);
    }

    public Cursor getTableArticle(String orderBy, int start, int num){
        String limit = String.format("%d, %d", start, num);
        return db.query(TABLE_NAME_ARTICLE, null, null, null, null, null, orderBy, limit);
    }

    public Cursor getTableArticle(String fromTime, String orderBy, int start, int num){
        String limit = String.format("%d, %d", start, num);
        return db.query(TABLE_NAME_ARTICLE, null, COL_DATE + " >=  ?", new String[]{fromTime}, null, null, orderBy, limit);
    }

    public Cursor getTableArticle(String orderby, int idSite){
        return db.query(TABLE_NAME_ARTICLE, null, COL_ID_SITE + " == ?", new String[]{String.valueOf(idSite)}, null, null, orderby);
    }

    public boolean isArticleDb(String url){
        boolean isArticleDb = false;

        Cursor cursor = db.query(TABLE_NAME_ARTICLE, null, COL_URL + " = ? ", new String[]{url}, null, null, null);
        if (cursor.moveToFirst()) {
            isArticleDb = true;
        }
        cursor.close();

        return isArticleDb;
    }

    public void updateReadArticle(String url){
        ContentValues values = new ContentValues();
        values.put(COL_READ, FLAG_READ);
        db.update(TABLE_NAME_ARTICLE, values, COL_URL + " = ?", new String[]{url});
    }

    public void DEBUG_DELETE_READ_ARTICLE () {
        ContentValues values = new ContentValues();
        values.put(COL_READ, FLAG_NO_READ);
        db.update(TABLE_NAME_ARTICLE, values, null, null);
    }

    @SuppressLint("Range")
    public int isReadArticle(String url){
        int isRead = FLAG_NO_READ;

        Cursor cursor = db.query(TABLE_NAME_ARTICLE, new String[]{COL_READ}, COL_URL + " = ? ", new String[]{url}, null, null, null);

        if (cursor.moveToFirst()) {
            isRead = cursor.getInt(cursor.getColumnIndex(COL_READ));
        }
        cursor.close();

        return isRead;
    }

    public void saveItemArticle(int id_site, String title,  String date, String url, int read){
        ContentValues values = new ContentValues();
        values.put(COL_ID_SITE, id_site);
        values.put(COL_TITLE, title);
        values.put(COL_DATE, date);
        values.put(COL_URL, url);
        values.put(COL_READ, read);
        db.insertWithOnConflict(TABLE_NAME_ARTICLE, null, values, CONFLICT_REPLACE);
    }

    //
    // Site Methods
    //
    public void  deleteTableSite(){
        db.execSQL("delete from " + TABLE_NAME_SITE);
        db.execSQL("delete from sqlite_sequence where name=\'" + TABLE_NAME_SITE + "\'");
    }

    public Cursor getTableSite(){
        return db.query(TABLE_NAME_SITE, null, null, null, null, null, null);
    }

    @SuppressLint("Range")
    public int getIdSite(String site) {
        int idSite = 0;

        Cursor cursor = db.query(TABLE_NAME_SITE, new String[]{COL_ID}, COL_SITE + " == ?", new String[]{site}, null, null, null);

        if (cursor.moveToFirst()) {
            idSite = cursor.getInt(cursor.getColumnIndex(COL_ID));
        }
        cursor.close();

        return idSite;
    }

    @SuppressLint("Range")
    public String getSite(int idsite) {
        String site = "";

        Cursor cursor = db.query(TABLE_NAME_SITE, new String[]{COL_SITE}, COL_ID + " == ?", new String[]{String.valueOf(idsite)}, null, null, null);

        if (cursor.moveToFirst()) {
            site = cursor.getString(cursor.getColumnIndex(COL_SITE));
        }
        cursor.close();

        return site;
    }

    public void saveItemSite(String site){
        ContentValues values = new ContentValues();
        values.put(COL_SITE, site);
        db.insertOrThrow(TABLE_NAME_SITE, null, values);
    }
}