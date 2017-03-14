package com.em_projects.infra.helpers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    protected SQLiteDatabase m_db;
    private String m_dbName;
    private String[] m_dbCreateCommands;

    public DatabaseHelper(Context context, String dbName, int dbVersion, String[] createCommands) {
        super(context, dbName, null, dbVersion);
        Log.d(TAG, "DatabaseHelper");
        m_dbName = dbName;
        m_dbCreateCommands = createCommands;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        for (String command : m_dbCreateCommands)
            db.execSQL(command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        Log.w(getClass().getSimpleName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + m_dbName);
        onCreate(db);
    }

    /**
     * Opens the database for read/write operations
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        Log.d(TAG, "open");
        m_db = this.getWritableDatabase();
    }

    /**
     * Closes the database
     */
    public void close() {
        Log.d(TAG, "close");
        this.close();
    }
}
