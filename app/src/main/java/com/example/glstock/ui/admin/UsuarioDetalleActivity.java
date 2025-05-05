package com.example.glstock.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.UsuarioService;
import com.example.glstock.model.Rol;
import com.example.glstock.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioDetalleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnEliminar;
    private Button btnGuardar;
    private Spinner spinnerRol;
    private TextInputLayout tilContrasena;
    private TextInputEditText etNombre;
    private TextInputEditText etApellido;
    private TextInputEditText etCorreo;
    private TextInputEditText etContrasena;
    private ProgressBar progressBar;

    private UsuarioService usuarioService;
    private Usuario usuarioActual;
    private boolean modoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_detalle);

        inicializarVistas();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        usuarioService = ApiClient.getClient().create(UsuarioService.class);

        List<String> roles = Arrays.asList("ADMIN", "GESTOR");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        if (getIntent().hasExtra("usuario_objeto")) {
            modoEdicion = true;
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario_objeto");
            if (usuarioActual != null) {
                mostrarDatosUsuario();
                getSupportActionBar().setTitle("Editar Usuario");
            }
        } else {
            modoEdicion = false;
            usuarioActual = new Usuario();
            getSupportActionBar().setTitle("Nuevo Usuario");
            btnEliminar.setVisibility(View.GONE);
            tilContrasena.setHint("Contraseña");
            etContrasena.setHint("Contraseña");
        }

        btnGuardar.setOnClickListener(v -> guardarUsuario());
        btnEliminar.setOnClickListener(v -> confirmarEliminarUsuario());
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnGuardar = findViewById(R.id.btnGuardar);
        spinnerRol = findViewById(R.id.spinnerRol);
        tilContrasena = findViewById(R.id.tilContrasena);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        progressBar = findViewById(R.id.progressBar);
    }

    private void mostrarDatosUsuario() {
        etNombre.setText(usuarioActual.getNombre());
        etApellido.setText(usuarioActual.getApellido());
        etCorreo.setText(usuarioActual.getCorreo());
        etContrasena.setText("");
        tilContrasena.setHint("Nueva contraseña (dejar vacío para mantener la actual)");
        etContrasena.setHint("Nueva contraseña (dejar vacío para mantener la actual)");

        if (usuarioActual.getRol() == Rol.ADMIN) {
            spinnerRol.setSelection(0);
        } else {
            spinnerRol.setSelection(1);
        }
    }

    private void guardarUsuario() {
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        if (TextUtils.isEmpty(etCorreo.getText())) {
            etCorreo.setError("El correo es obligatorio");
            return;
        }

        if (!modoEdicion && TextUtils.isEmpty(etContrasena.getText())) {
            etContrasena.setError("La contraseña es obligatoria");
            return;
        }

        usuarioActual.setNombre(etNombre.getText().toString());
        usuarioActual.setApellido(etApellido.getText().toString());
        usuarioActual.setCorreo(etCorreo.getText().toString());

        String contrasena = etContrasena.getText().toString();
        if (!TextUtils.isEmpty(contrasena)) {
            usuarioActual.setContrasena(contrasena);
        }

        usuarioActual.setRol(spinnerRol.getSelectedItemPosition() == 0 ? Rol.ADMIN : Rol.GESTOR);

        // 👇 Asegurarse de conservar la fecha de creación si estamos editando
        if (modoEdicion && usuarioActual.getFechaCreacion() != null) {
            usuarioActual.setFechaCreacion(usuarioActual.getFechaCreacion());
        }

        showProgress(true);

        Call<Usuario> call;
        if (modoEdicion) {
            call = usuarioService.actualizarUsuario(usuarioActual.getId(), usuarioActual);
        } else {
            call = usuarioService.crearUsuario(usuarioActual);
        }

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Usuario guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Error al guardar usuario: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                showProgress(false);
                Toast.makeText(UsuarioDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminarUsuario() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Está seguro que desea eliminar este usuario?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarUsuario())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuario() {
        if (usuarioActual == null || usuarioActual.getId() == null) {
            Toast.makeText(this, "No se puede eliminar el usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        usuarioService.eliminarUsuario(usuarioActual.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Error al eliminar usuario: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProgress(false);
                Toast.makeText(UsuarioDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!show);
        btnEliminar.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}