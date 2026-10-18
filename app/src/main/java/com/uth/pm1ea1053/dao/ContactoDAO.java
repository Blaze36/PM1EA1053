package com.uth.pm1ea1053.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uth.pm1ea1053.model.Contacto;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO: único lugar que habla con SQLite. Todas las consultas usan parámetros.
 */
public class ContactoDAO {

    private final DbHelper helper;

    public ContactoDAO(Context context) {
        this.helper = new DbHelper(context.getApplicationContext());
    }

    /** Inserta un contacto (incluida la ruta de su fotografía). Devuelve el id o -1 si falla. */
    public long insertar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(DbHelper.TABLA, null, valores(c));
    }

    /** Actualiza el contacto indicado. Devuelve la cantidad de filas afectadas. */
    public int actualizar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(DbHelper.TABLA, valores(c),
                DbHelper.COL_ID + " = ?", new String[]{String.valueOf(c.getId())});
    }

    /** Elimina el contacto con el id indicado. Devuelve la cantidad de filas afectadas. */
    public int eliminar(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DbHelper.TABLA,
                DbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** Busca un contacto por id; devuelve null si no existe. */
    public Contacto obtener(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLA, null,
                DbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return desdeCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /** Lista todos los contactos guardados, ordenados por nombre. */
    public List<Contacto> listar() {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.TABLA, null, null, null, null, null,
                DbHelper.COL_NOMBRE + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                lista.add(desdeCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return lista;
    }

    private ContentValues valores(Contacto c) {
        ContentValues v = new ContentValues();
        v.put(DbHelper.COL_PAIS, c.getPais());
        v.put(DbHelper.COL_NOMBRE, c.getNombre());
        v.put(DbHelper.COL_TELEFONO, c.getTelefono());
        v.put(DbHelper.COL_NOTA, c.getNota());
        v.put(DbHelper.COL_IMAGEN, c.getImagen());
        return v;
    }

    private Contacto desdeCursor(Cursor cur) {
        Contacto c = new Contacto();
        c.setId(cur.getLong(cur.getColumnIndexOrThrow(DbHelper.COL_ID)));
        c.setPais(cur.getString(cur.getColumnIndexOrThrow(DbHelper.COL_PAIS)));
        c.setNombre(cur.getString(cur.getColumnIndexOrThrow(DbHelper.COL_NOMBRE)));
        c.setTelefono(cur.getString(cur.getColumnIndexOrThrow(DbHelper.COL_TELEFONO)));
        c.setNota(cur.getString(cur.getColumnIndexOrThrow(DbHelper.COL_NOTA)));
        c.setImagen(cur.getString(cur.getColumnIndexOrThrow(DbHelper.COL_IMAGEN)));
        return c;
    }
}
