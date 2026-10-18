package com.uth.pm1ea1053.view;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uth.pm1ea1053.R;
import com.uth.pm1ea1053.controller.ContactoController;
import com.uth.pm1ea1053.model.Contacto;
import com.uth.pm1ea1053.util.ImageUtils;

/**
 * Pantalla 3: llamar a un contacto.
 * Flujo: botón Llamar -> AlertDialog de confirmación -> permiso CALL_PHONE en tiempo
 * de ejecución -> ACTION_CALL.
 */
public class LlamarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "contacto_id";

    private Contacto contacto;

    private final ActivityResultLauncher<String> permisoLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (Boolean.TRUE.equals(concedido)) {
                    realizarLlamada();
                } else {
                    mostrarPermisoDenegado();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ContactoController controller = new ContactoController(this);
        contacto = controller.obtener(getIntent().getLongExtra(EXTRA_ID, 0));
        if (contacto == null) {
            Toast.makeText(this, R.string.msg_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView ivFoto = findViewById(R.id.ivFotoLlamar);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvPais = findViewById(R.id.tvPais);
        TextView tvTelefono = findViewById(R.id.tvTelefono);
        TextView tvNota = findViewById(R.id.tvNota);
        Button btnLlamar = findViewById(R.id.btnLlamar);

        tvNombre.setText(contacto.getNombre());
        tvPais.setText(contacto.getPais());
        tvTelefono.setText(contacto.getTelefono());
        if (contacto.getNota() != null && !contacto.getNota().isEmpty()) {
            tvNota.setText(contacto.getNota());
            tvNota.setVisibility(TextView.VISIBLE);
        }

        Bitmap bitmap = ImageUtils.cargar(contacto.getImagen(), 600);
        if (bitmap != null) {
            ivFoto.setImageBitmap(bitmap);
        }

        btnLlamar.setOnClickListener(v -> confirmarLlamada());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Paso 1: confirmar con un AlertDialog.Builder antes de marcar. */
    private void confirmarLlamada() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_llamada_titulo)
                .setMessage(getString(R.string.confirmar_llamada,
                        contacto.getNombre(), contacto.getTelefono()))
                .setPositiveButton(R.string.btn_llamar, (dialog, which) -> verificarPermiso())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /** Paso 2: solicitar CALL_PHONE en tiempo de ejecución si todavía no fue concedido. */
    private void verificarPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            realizarLlamada();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.CALL_PHONE)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permiso_titulo)
                    .setMessage(R.string.permiso_explicacion)
                    .setPositiveButton(R.string.conceder,
                            (dialog, which) -> permisoLauncher.launch(Manifest.permission.CALL_PHONE))
                    .setNegativeButton(R.string.cancelar, null)
                    .show();
        } else {
            permisoLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    /** Si el usuario niega el permiso: se explica y, si es definitivo, se ofrece abrir los ajustes. */
    private void mostrarPermisoDenegado() {
        boolean definitivo = !ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.CALL_PHONE);
        if (definitivo) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permiso_denegado_titulo)
                    .setMessage(R.string.permiso_denegado_msg)
                    .setPositiveButton(R.string.abrir_ajustes, (dialog, which) -> {
                        Intent ajustes = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        startActivity(ajustes);
                    })
                    .setNegativeButton(R.string.cancelar, null)
                    .show();
        } else {
            Toast.makeText(this, R.string.msg_permiso_denegado, Toast.LENGTH_SHORT).show();
        }
    }

    /** Paso 3: marcar con ACTION_CALL (llama directamente, por eso requiere CALL_PHONE). */
    private void realizarLlamada() {
        Intent llamada = new Intent(Intent.ACTION_CALL,
                Uri.fromParts("tel", contacto.getTelefono(), null));
        try {
            startActivity(llamada);
        } catch (SecurityException | ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_no_llamada, Toast.LENGTH_SHORT).show();
        }
    }
}
