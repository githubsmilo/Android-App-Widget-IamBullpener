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
    
    public void insert(String title, String writer, String url, int starred) {
        ContentValues values = new ContentValues();
        values.put(ScrapArticleColumns.TITLE, title);
        values.put(ScrapArticleColumns.WRITER, writer);
        values.put(ScrapArticleColumns.URL, url);
        values.put(ScrapArticleColumns.STARRED, starred);
        mDb.insert(mHelper.getScrapListTableName(), null, values);
    }
    
    public void update(String url, int starred) {
        ContentValues values = new ContentValues();
        values.put(ScrapArticleColumns.STARRED, starred);
        mDb.update(mHelper.getScrapListTableName(), values, ScrapArticleColumns.URL + "=?", new String[]{url});
    }
    
    public void delete(String url) {
        mDb.delete(mHelper.getScrapListTableName(), ScrapArticleColumns.URL + "=?", new String[]{url});
    }
    
    public Cursor select() {
        Cursor c = mDb.query(mHelper.getScrapListTableName(), null, null, null, null, null, null);
        return c;
    }
}
