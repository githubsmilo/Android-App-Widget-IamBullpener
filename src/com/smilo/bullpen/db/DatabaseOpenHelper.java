package com.smilo.bullpen.db;

import com.smilo.bullpen.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseOpenHelper";
    private static final boolean DEBUG = Constants.DEBUG_MODE;
    
    private static final String DATABASE_NAME = "bullpen_database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SCRAP_LIST_TABLE_NAME ="scrap_list";
    
    // Query
    private static final String CREATE_SCRAP_LIST_TABLE =
            "CREATE TABLE " + SCRAP_LIST_TABLE_NAME + " (" +
                    ScrapArticleColumns._ID + " INTEGER PRIMARY KEY, " +
                    ScrapArticleColumns.TITLE + " TEXT, " +
                    ScrapArticleColumns.WRITER + " TEXT, " +
                    ScrapArticleColumns.URL + " TEXT)";
    private static final String DROP_SCRAP_LIST_TABLE =
            "DROP TABLE IF EXISTS " + SCRAP_LIST_TABLE_NAME;
    
    public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SCRAP_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG) Log.i(TAG, "Upgrade is not permited! This process will destroy all old data!");
        db.execSQL(DROP_SCRAP_LIST_TABLE);
        db.execSQL(CREATE_SCRAP_LIST_TABLE);
    }
    
    public String getScrapListTableName() {
        return SCRAP_LIST_TABLE_NAME;
    }
    
    public static final class ScrapArticleColumns implements BaseColumns {
        
        private ScrapArticleColumns() {}
        
        // Column names
        public static final String TITLE = "title";
        public static final String WRITER = "writer";
        public static final String URL = "url";
        
        public static final String[] PROJECTION = {
           BaseColumns._ID,
           TITLE,
           WRITER,
           URL
        };
        
        public static final int ID_INDEX = 0;
        public static final int TITLE_INDEX = 1;
        public static final int WRITER_INDEX = 2;
        public static final int URL_INDEX = 3;
    }
}
