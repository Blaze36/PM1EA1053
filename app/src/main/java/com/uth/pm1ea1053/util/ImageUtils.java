package com.uth.pm1ea1053.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilidades para guardar, cargar y compartir las fotografías de los contactos.
 * Las fotos viven en el almacenamiento interno de la app: filesDir/fotos/.
 */
public final class ImageUtils {

    private static final String CARPETA = "fotos";

    private ImageUtils() {
    }

    public static File carpetaFotos(Context context) {
        File dir = new File(context.getFilesDir(), CARPETA);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** Devuelve un archivo nuevo (aún sin contenido) dentro de la carpeta de fotos. */
    public static File crearArchivoFoto(Context context) {
        String nombre = "IMG_" + System.currentTimeMillis() + ".jpg";
        return new File(carpetaFotos(context), nombre);
    }

    /** Uri content:// del archivo, apta para la cámara y para compartir con otras apps. */
    public static Uri uriCompartible(Context context, String ruta) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", new File(ruta));
    }

    /** Copia una imagen elegida de la galería a la carpeta de la app. Devuelve la ruta o null. */
    public static String copiarDesdeUri(Context context, Uri origen) {
        File destino = crearArchivoFoto(context);
        try (InputStream in = context.getContentResolver().openInputStream(origen);
             OutputStream out = new FileOutputStream(destino)) {
            if (in != null) {
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = in.read(buffer)) > 0) {
                    out.write(buffer, 0, leidos);
                }
                return destino.getAbsolutePath();
            }
        } catch (IOException | SecurityException e) {
            // se limpia abajo
        }
        destino.delete();
        return null;
    }

    /**
     * Carga la imagen reducida (lado mayor aproximado = maxLado) y la gira según su EXIF.
     * Devuelve null si la ruta es nula, el archivo no existe o no se puede decodificar.
     */
    public static Bitmap cargar(String ruta, int maxLado) {
        if (ruta == null) {
            return null;
        }
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return null;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, opciones);
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            return null;
        }

        int ladoMayor = Math.max(opciones.outWidth, opciones.outHeight);
        int muestreo = 1;
        while (ladoMayor / (muestreo * 2) >= maxLado) {
            muestreo *= 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = muestreo;
        Bitmap bitmap = BitmapFactory.decodeFile(ruta, opciones);
        if (bitmap == null) {
            return null;
        }
        return rotarSegunExif(ruta, bitmap);
    }

    /** Borra el archivo de la imagen (si existe). */
    public static void eliminar(String ruta) {
        if (ruta != null) {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                archivo.delete();
            }
        }
    }

    private static Bitmap rotarSegunExif(String ruta, Bitmap bitmap) {
        int grados = 0;
        try {
            ExifInterface exif = new ExifInterface(ruta);
            int orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    grados = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    grados = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    grados = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            return bitmap;
        }

        if (grados == 0) {
            return bitmap;
        }
        Matrix matriz = new Matrix();
        matriz.postRotate(grados);
        Bitmap girado = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matriz, true);
        if (girado != bitmap) {
            bitmap.recycle();
        }
        return girado;
    }
}
