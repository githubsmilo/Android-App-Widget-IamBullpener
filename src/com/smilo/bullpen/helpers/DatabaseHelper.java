package com.smilo.bullpen.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smilo.bullpen.db.Database;
import com.smilo.bullpen.db.Database.ScrapArticleColumns;
import com.smilo.bullpen.definitions.Constants;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    Database mDatabase;
    SQLiteDatabase mDb;
    
    public DatabaseHelper(Context context) {
        mDatabase = new Database(context, null, null, 1);
        mDb = mDatabase.getWritableDatabase();
    }
    
    public static DatabaseHelper open(Context context) {
        return new DatabaseHelper(context);
    }
    
    public void close() {
        if (mDb != null) {
            mDb.close();
        }
    }
    
    public void insert(String title, String writer, String url) {
        ContentValues values = new ContentValues();
        values.put(ScrapArticleColumns.TITLE, title);
        values.put(ScrapArticleColumns.WRITER, writer);
        values.put(ScrapArticleColumns.URL, url);
        mDb.insert(mDatabase.getScrapListTableName(), null, values);
    }
    
    public void delete(String url) {
        mDb.delete(mDatabase.getScrapListTableName(), ScrapArticleColumns.URL + "=?", new String[]{url});
    }
    
    public void star(String url) {
        
    }
    
    public Cursor selectAll() {
        Cursor c = mDb.query(mDatabase.getScrapListTableName(), null, null, null, null, null, ScrapArticleColumns._ID + " DESC");
        return c;
    }
    
    public Cursor selectUrl(String url) {
        Cursor c = mDb.query(mDatabase.getScrapListTableName(), null, ScrapArticleColumns.URL + "=?", new String[]{url}, null, null, null);
        return c;
    }
}
