package com.nam.vngnearbyplaces;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Nam on 29/12/2015.
 */
public class LocationReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Location.db";
    private String TEXT_TYPE;
    private String COMMA_SEP;
    private String SQL_CREATE_ENTRIES;
    private String SQL_DELETE_ENTRIES;

//    public void setTableName(String tableName) {
//        this.tableName = tableName;
//    }

//    private static String tableName;

    /* Inner class that defines the table contents */
    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_REFERENCE = "reference";
        public static final String COLUMN_NULLABLE = "distance";
    }

    public LocationReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        this.tableName = tableName;
        init();
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    private void init() {
        TEXT_TYPE = " TEXT";
        COMMA_SEP = ",";
        SQL_CREATE_ENTRIES =
                "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                        LocationEntry._ID + " INTEGER PRIMARY KEY," +
                        LocationEntry.COLUMN_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_ICON + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_TYPE + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_DISTANCE + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_LAT + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_LNG + TEXT_TYPE + COMMA_SEP +
                        LocationEntry.COLUMN_REFERENCE + TEXT_TYPE +
                        " )";

        SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;
    }
}