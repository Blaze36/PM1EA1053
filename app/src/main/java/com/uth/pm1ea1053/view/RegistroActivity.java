package com.uth.pm1ea1053.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uth.pm1ea1053.R;
import com.uth.pm1ea1053.controller.ContactoController;
import com.uth.pm1ea1053.model.Contacto;
import com.uth.pm1ea1053.util.ImageUtils;

import java.io.File;
import java.util.Map;

/**
 * Pantalla 1: Registro de contactos (también se usa para actualizar un contacto existente).
 */
public class RegistroActivity extends AppCompatActivity {

    /** Si llega este extra (id de contacto), la pantalla funciona en modo "actualizar". */
    public static final String EXTRA_ID = "contacto_id";

    private static final String ESTADO_ID = "estado_id";
    private static final String ESTADO_RUTA = "estado_ruta";
    private static final String ESTADO_ORIGINAL = "estado_original";
    private static final String ESTADO_TEMPORAL = "estado_temporal";

    private ImageView ivFoto;
    private Spinner spPais;
    private TextInputLayout tilNombre;
    private TextInputLayout tilTelefono;
    private TextInputEditText etNombre;
    private TextInputEditText etTelefono;
    private TextInputEditText etNota;
    private Button btnGuardar;
    private ArrayAdapter<CharSequence> adapterPais;

    private ContactoController controller;
    private long contactoId = 0;
    private String rutaImagen;     // imagen actualmente elegida (puede ser nueva)
    private String rutaOriginal;   // imagen que ya está guardada en la BD (modo actualizar)
    private String rutaTemporal;   // archivo donde la cámara está escribiendo
    private boolean guardado = false;

    private final ActivityResultLauncher<Uri> camaraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), ok -> {
                if (rutaTemporal == null) {
                    return;
                }
                if (Boolean.TRUE.equals(ok)) {
                    reemplazarImagen(rutaTemporal);
                } else {
                    ImageUtils.eliminar(rutaTemporal);
                }
                rutaTemporal = null;
            });

    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                String ruta = ImageUtils.copiarDesdeUri(this, uri);
                if (ruta == null) {
                    Toast.makeText(this, R.string.msg_error_imagen, Toast.LENGTH_SHORT).show();
                } else {
                    reemplazarImagen(ruta);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        controller = new ContactoController(this);

        ivFoto = findViewById(R.id.ivFoto);
        spPais = findViewById(R.id.spPais);
        tilNombre = findViewById(R.id.tilNombre);
        tilTelefono = findViewById(R.id.tilTelefono);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        btnGuardar = findViewById(R.id.btnGuardar);
        Button btnCamara = findViewById(R.id.btnCamara);
        Button btnGaleria = findViewById(R.id.btnGaleria);
        Button btnVerContactos = findViewById(R.id.btnVerContactos);

        adapterPais = ArrayAdapter.createFromResource(
                this, R.array.paises, android.R.layout.simple_spinner_item);
        adapterPais.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPais.setAdapter(adapterPais);

        if (savedInstanceState != null) {
            contactoId = savedInstanceState.getLong(ESTADO_ID, 0);
            rutaImagen = savedInstanceState.getString(ESTADO_RUTA);
            rutaOriginal = savedInstanceState.getString(ESTADO_ORIGINAL);
            rutaTemporal = savedInstanceState.getString(ESTADO_TEMPORAL);
        } else {
            contactoId = getIntent().getLongExtra(EXTRA_ID, 0);
            if (contactoId != 0) {
                cargarContacto();
            }
        }

        if (contactoId != 0) {
            setTitle(R.string.titulo_actualizar);
            btnGuardar.setText(R.string.btn_actualizar);
            btnVerContactos.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            setTitle(R.string.titulo_registro);
        }
        mostrarFoto();

        btnCamara.setOnClickListener(v -> abrirCamara());
        btnGaleria.setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardar());
        btnVerContactos.setOnClickListener(v ->
                startActivity(new Intent(this, ListaActivity.class)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ESTADO_ID, contactoId);
        outState.putString(ESTADO_RUTA, rutaImagen);
        outState.putString(ESTADO_ORIGINAL, rutaOriginal);
        outState.putString(ESTADO_TEMPORAL, rutaTemporal);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Si el usuario eligió una foto nueva y salió sin guardar, se borra el archivo huérfano.
        if (isFinishing() && !guardado && rutaImagen != null && !rutaImagen.equals(rutaOriginal)) {
            ImageUtils.eliminar(rutaImagen);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ------------------------------------------------------------------ modo actualizar

    private void cargarContacto() {
        Contacto c = controller.obtener(contactoId);
        if (c == null) {
            Toast.makeText(this, R.string.msg_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etNombre.setText(c.getNombre());
        etTelefono.setText(c.getTelefono());
        etNota.setText(c.getNota());
        spPais.setSelection(indicePais(c.getPais()));
        rutaOriginal = c.getImagen();
        rutaImagen = rutaOriginal;
    }

    private int indicePais(String pais) {
        for (int i = 0; i < adapterPais.getCount(); i++) {
            CharSequence item = adapterPais.getItem(i);
            if (item != null && item.toString().equals(pais)) {
                return i;
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------ fotografía

    private void abrirCamara() {
        File archivo = ImageUtils.crearArchivoFoto(this);
        rutaTemporal = archivo.getAbsolutePath();
        Uri uri = ImageUtils.uriCompartible(this, rutaTemporal);
        try {
            camaraLauncher.launch(uri);
        } catch (ActivityNotFoundException e) {
            ImageUtils.eliminar(rutaTemporal);
            rutaTemporal = null;
            Toast.makeText(this, R.string.msg_sin_camara, Toast.LENGTH_SHORT).show();
        }
    }

    private void reemplazarImagen(String nuevaRuta) {
        // Si la imagen actual era una elegida en esta sesión (no la guardada), se descarta.
        if (rutaImagen != null && !rutaImagen.equals(rutaOriginal)) {
            ImageUtils.eliminar(rutaImagen);
        }
        rutaImagen = nuevaRuta;
        mostrarFoto();
    }

    private void mostrarFoto() {
        Bitmap bitmap = ImageUtils.cargar(rutaImagen, 600);
        if (bitmap != null) {
            ivFoto.setImageBitmap(bitmap);
        } else {
            ivFoto.setImageResource(R.drawable.ic_person);
        }
    }

    // ------------------------------------------------------------------ guardar

    private void guardar() {
        Contacto c = new Contacto();
        c.setId(contactoId);
        c.setPais(spPais.getSelectedItemPosition() > 0
                ? String.valueOf(spPais.getSelectedItem()) : "");
        c.setNombre(texto(etNombre));
        c.setTelefono(texto(etTelefono));
        c.setNota(texto(etNota));
        c.setImagen(rutaImagen);

        Map<String, String> errores = controller.validar(c);
        if (!errores.isEmpty()) {
            mostrarErrores(errores);
            return;
        }

        tilNombre.setError(null);
        tilTelefono.setError(null);

        if (!controller.guardar(c)) {
            Toast.makeText(this, R.string.msg_error_guardar, Toast.LENGTH_SHORT).show();
            return;
        }

        if (contactoId != 0) {
            // La foto anterior ya no se usa si fue reemplazada.
            if (rutaOriginal != null && !rutaOriginal.equals(rutaImagen)) {
                ImageUtils.eliminar(rutaOriginal);
            }
            guardado = true;
            Toast.makeText(this, R.string.msg_actualizado, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.msg_guardado, Toast.LENGTH_SHORT).show();
            limpiarFormulario();
        }
    }

    /** Marca los campos con error y muestra una alerta con todos los problemas encontrados. */
    private void mostrarErrores(Map<String, String> errores) {
        tilNombre.setError(errores.get(ContactoController.CAMPO_NOMBRE));
        tilTelefono.setError(errores.get(ContactoController.CAMPO_TELEFONO));

        StringBuilder mensaje = new StringBuilder();
        for (String error : errores.values()) {
            if (mensaje.length() > 0) {
                mensaje.append("\n");
            }
            mensaje.append("• ").append(error);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.alerta_titulo)
                .setMessage(mensaje.toString())
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        spPais.setSelection(0);
        rutaImagen = null;
        rutaOriginal = null;
        mostrarFoto();
        etNombre.requestFocus();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
