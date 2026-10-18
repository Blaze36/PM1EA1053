package com.uth.pm1ea1053.view;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.uth.pm1ea1053.R;
import com.uth.pm1ea1053.controller.ContactoController;
import com.uth.pm1ea1053.model.Contacto;
import com.uth.pm1ea1053.util.ImageUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Pantalla 2: lista de contactos guardados.
 * Al tocar un contacto: ver imagen, compartir, llamar, actualizar o eliminar.
 */
public class ListaActivity extends AppCompatActivity {

    private ContactoController controller;
    private ContactoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ContactoController(this);
        adapter = new ContactoAdapter(this, new ArrayList<>());

        ListView lista = findViewById(R.id.lista);
        TextView tvVacio = findViewById(R.id.tvVacio);
        lista.setAdapter(adapter);
        lista.setEmptyView(tvVacio);
        lista.setOnItemClickListener((parent, view, position, id) -> {
            Contacto c = adapter.getItem(position);
            if (c != null) {
                mostrarOpciones(c);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos(); // refresca al volver de actualizar un contacto
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Lee los contactos de la base de datos y refresca la lista. */
    private void cargarContactos() {
        adapter.clear();
        adapter.addAll(controller.listar());
    }

    private void mostrarOpciones(Contacto c) {
        String[] opciones = {
                getString(R.string.opcion_ver_imagen),
                getString(R.string.opcion_compartir),
                getString(R.string.opcion_llamar),
                getString(R.string.opcion_actualizar),
                getString(R.string.opcion_eliminar)
        };

        new AlertDialog.Builder(this)
                .setTitle(c.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            verImagen(c);
                            break;
                        case 1:
                            compartir(c);
                            break;
                        case 2:
                            llamar(c);
                            break;
                        case 3:
                            actualizar(c);
                            break;
                        case 4:
                            confirmarEliminar(c);
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    // ------------------------------------------------------------------ ver imagen

    private void verImagen(Contacto c) {
        Bitmap bitmap = ImageUtils.cargar(c.getImagen(), 1280);
        if (bitmap == null) {
            Toast.makeText(this, R.string.msg_sin_imagen, Toast.LENGTH_SHORT).show();
            return;
        }

        View contenido = getLayoutInflater().inflate(R.layout.dialog_imagen, null);
        ImageView ivGrande = contenido.findViewById(R.id.ivGrande);
        ivGrande.setImageBitmap(bitmap);

        new AlertDialog.Builder(this)
                .setTitle(c.getNombre())
                .setView(contenido)
                .setPositiveButton(R.string.cerrar, null)
                .show();
    }

    // ------------------------------------------------------------------ compartir (ACTION_SEND)

    private void compartir(Contacto c) {
        String texto = getString(R.string.compartir_texto,
                c.getNombre(), c.getTelefono(), c.getPais());
        if (c.getNota() != null && !c.getNota().isEmpty()) {
            texto += getString(R.string.compartir_nota, c.getNota());
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, c.getNombre());
        intent.putExtra(Intent.EXTRA_TEXT, texto);

        boolean hayImagen = c.getImagen() != null && new File(c.getImagen()).exists();
        if (hayImagen) {
            Uri uri = ImageUtils.uriCompartible(this, c.getImagen());
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setClipData(ClipData.newRawUri("imagen", uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
        }

        startActivity(Intent.createChooser(intent, getString(R.string.compartir_titulo)));
    }

    // ------------------------------------------------------------------ llamar / actualizar

    private void llamar(Contacto c) {
        Intent intent = new Intent(this, LlamarActivity.class);
        intent.putExtra(LlamarActivity.EXTRA_ID, c.getId());
        startActivity(intent);
    }

    private void actualizar(Contacto c) {
        Intent intent = new Intent(this, RegistroActivity.class);
        intent.putExtra(RegistroActivity.EXTRA_ID, c.getId());
        startActivity(intent);
    }

    // ------------------------------------------------------------------ eliminar

    private void confirmarEliminar(Contacto c) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(getString(R.string.confirmar_eliminar, c.getNombre()))
                .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                    if (controller.eliminar(c)) {
                        Toast.makeText(this, R.string.msg_eliminado, Toast.LENGTH_SHORT).show();
                        cargarContactos();
                    } else {
                        Toast.makeText(this, R.string.msg_error_eliminar, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
