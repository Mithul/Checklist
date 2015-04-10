package com.example.mithul.checklist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mithul on 9/4/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ChecklistKoreTask";
    private static final java.lang.String DATABASE_CREATE = "";
    private static final String TABLE_COMMENTS = "";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        onCreate(database);
    }
}