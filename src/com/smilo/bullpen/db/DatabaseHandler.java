package com.smilo.bullpen.db;

import com.smilo.bullpen.Constants;
import com.smilo.bullpen.db.DatabaseOpenHelper.ScrapArticleColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHandler {

    private static final String TAG = "DatabaseHandler";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    DatabaseOpenHelper mHelper;
    SQLiteDatabase mDb;
    
    public DatabaseHandler(Context context) {
        mHelper = new DatabaseOpenHelper(context, null, null, 1);
        mDb = mHelper.getWritableDatabase();
    }
    
    public static DatabaseHandler open(Context context) {
        return new DatabaseHandler(context);
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
        mDb.insert(mHelper.getScrapListTableName(), null, values);
    }
    
    public void delete(String url) {
        mDb.delete(mHelper.getScrapListTableName(), ScrapArticleColumns.URL + "=?", new String[]{url});
    }
    
    public void star(String url) {
        
    }
    
    public Cursor selectAll() {
        Cursor c = mDb.query(mHelper.getScrapListTableName(), null, null, null, null, null, ScrapArticleColumns._ID + " DESC");
        return c;
    }
    
    public Cursor selectUrl(String url) {
        Cursor c = mDb.query(mHelper.getScrapListTableName(), null, ScrapArticleColumns.URL + "=?", new String[]{url}, null, null, null);
        return c;
    }
}
