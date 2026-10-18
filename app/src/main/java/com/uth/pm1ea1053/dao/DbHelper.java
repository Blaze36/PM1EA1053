package com.uth.pm1ea1053.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Crea y versiona la base de datos SQLite de la agenda.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "agenda.db";
    public static final int DB_VERSION = 1;

    public static final String TABLA = "contactos";
    public static final String COL_ID = "id";
    public static final String COL_PAIS = "pais";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_TELEFONO = "telefono";
    public static final String COL_NOTA = "nota";
    public static final String COL_IMAGEN = "imagen";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PAIS + " TEXT NOT NULL, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_TELEFONO + " TEXT NOT NULL, "
                + COL_NOTA + " TEXT, "
                + COL_IMAGEN + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }
}
