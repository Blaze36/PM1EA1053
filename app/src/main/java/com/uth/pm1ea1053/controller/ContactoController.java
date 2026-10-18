package com.uth.pm1ea1053.controller;

import android.content.Context;

import com.uth.pm1ea1053.dao.ContactoDAO;
import com.uth.pm1ea1053.model.Contacto;
import com.uth.pm1ea1053.util.ImageUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Controlador (C de MVC): valida los datos y coordina las vistas con el DAO.
 */
public class ContactoController {

    // Claves de los campos validados (se usan para mostrar el error en el campo correcto)
    public static final String CAMPO_PAIS = "pais";
    public static final String CAMPO_NOMBRE = "nombre";
    public static final String CAMPO_TELEFONO = "telefono";
    public static final String CAMPO_IMAGEN = "imagen";

    /** Nombre: solo letras (con tildes y ñ), palabras separadas por un único espacio. */
    public static final Pattern PATRON_NOMBRE = Pattern.compile("^\\p{L}+( \\p{L}+)*$");

    /** Teléfono: exactamente 8 dígitos. */
    public static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{8}$");

    private final ContactoDAO dao;

    public ContactoController(Context context) {
        this.dao = new ContactoDAO(context);
    }

    /**
     * Valida el contacto. Devuelve un mapa campo -> mensaje de error;
     * si está vacío, los datos son válidos.
     */
    public Map<String, String> validar(Contacto c) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (vacio(c.getPais())) {
            errores.put(CAMPO_PAIS, "Seleccione un país");
        }

        if (vacio(c.getNombre())) {
            errores.put(CAMPO_NOMBRE, "El nombre es obligatorio");
        } else if (!PATRON_NOMBRE.matcher(c.getNombre().trim()).matches()) {
            errores.put(CAMPO_NOMBRE, "El nombre solo puede contener letras");
        }

        if (vacio(c.getTelefono())) {
            errores.put(CAMPO_TELEFONO, "El teléfono es obligatorio");
        } else if (!PATRON_TELEFONO.matcher(c.getTelefono().trim()).matches()) {
            errores.put(CAMPO_TELEFONO, "El teléfono debe tener exactamente 8 dígitos");
        }

        if (vacio(c.getImagen())) {
            errores.put(CAMPO_IMAGEN, "Debe agregar una fotografía");
        }

        return errores;
    }

    /** Inserta (id == 0) o actualiza (id != 0) el contacto. Asume que ya fue validado. */
    public boolean guardar(Contacto c) {
        c.setPais(c.getPais().trim());
        c.setNombre(c.getNombre().trim());
        c.setTelefono(c.getTelefono().trim());
        c.setNota(c.getNota() == null ? "" : c.getNota().trim());

        if (c.getId() == 0) {
            long id = dao.insertar(c);
            if (id > 0) {
                c.setId(id);
                return true;
            }
            return false;
        }
        return dao.actualizar(c) > 0;
    }

    public Contacto obtener(long id) {
        return dao.obtener(id);
    }

    public List<Contacto> listar() {
        return dao.listar();
    }

    /** Elimina el contacto de la base de datos y borra el archivo de su fotografía. */
    public boolean eliminar(Contacto c) {
        boolean ok = dao.eliminar(c.getId()) > 0;
        if (ok) {
            ImageUtils.eliminar(c.getImagen());
        }
        return ok;
    }

    private boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
