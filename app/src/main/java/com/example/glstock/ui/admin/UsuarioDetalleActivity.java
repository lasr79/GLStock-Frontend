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

// Actividad que permite crear, editar, visualizar o eliminar un usuario
public class UsuarioDetalleActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button btnEliminar, btnGuardar;
    private Spinner spinnerRol;
    private TextInputLayout tilContrasena;
    private TextInputEditText etNombre, etApellido, etCorreo, etContrasena;
    private ProgressBar progressBar;
    private UsuarioService usuarioService;
    // Usuario que se está editando o visualizando
    private Usuario usuarioActual;
    // Verdadero si se esta editando un usuario existente
    private boolean modoEdicion = false;
    // Verdadero si está en modo solo visualizacion
    private boolean soloLectura = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_detalle); // Carga el layout
    // Vincula las vistas del XML
        inicializarVistas();
        // Configura el toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        usuarioService = ApiClient.getClient().create(UsuarioService.class);
        // Lista de roles disponibles
        List<String> roles = Arrays.asList("ADMIN", "GESTOR");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Asocia los roles al spinner
        spinnerRol.setAdapter(adapter);
        // Verifica si se recibio el modo solo lectura (GESTOR)
        if (getIntent().hasExtra("solo_lectura")) {
            soloLectura = getIntent().getBooleanExtra("solo_lectura", false);
        }
        // Verifica si se paso un usuario para editar (ADMIN)
        if (getIntent().hasExtra("usuario_objeto")) {
            modoEdicion = true;
            usuarioActual = (Usuario) getIntent().getSerializableExtra("usuario_objeto");
            if (usuarioActual != null) {
                mostrarDatosUsuario(); // Muestra los datos en pantalla
                getSupportActionBar().setTitle(soloLectura ? "Detalle de Usuario" : "Editar Usuario");
            }
        } else {
            // Es un nuevo usuario
            modoEdicion = false;
            usuarioActual = new Usuario();
            getSupportActionBar().setTitle("Nuevo Usuario");
            btnEliminar.setVisibility(View.GONE);
            tilContrasena.setHint("Contraseña");
            etContrasena.setHint("Contraseña");
        }
        // Si es solo lectura, desactiva la edicion
        if (soloLectura) {
            desactivarEdicion();
        }
        // Listeners para los botones
        btnGuardar.setOnClickListener(v -> guardarUsuario());
        btnEliminar.setOnClickListener(v -> confirmarEliminarUsuario());
    }
    // Vincula cada vista del layout con su variable
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
    // Muestra los datos del usuario en los campos de texto
    private void mostrarDatosUsuario() {
        etNombre.setText(usuarioActual.getNombre());
        etApellido.setText(usuarioActual.getApellido());
        etCorreo.setText(usuarioActual.getCorreo());
        etContrasena.setText("");
        tilContrasena.setHint("Nueva contraseña (dejar vacío para mantener la actual)");
        etContrasena.setHint("Nueva contraseña (dejar vacío para mantener la actual)");
        // Selecciona el rol correspondiente en el spinner
        if (usuarioActual.getRol() == Rol.ADMIN) {
            spinnerRol.setSelection(0);
        } else {
            spinnerRol.setSelection(1);
        }
    }
    // Desactiva la edicion de todos los campos
    private void desactivarEdicion() {
        etNombre.setEnabled(false);
        etApellido.setEnabled(false);
        etCorreo.setEnabled(false);
        etContrasena.setEnabled(false);
        spinnerRol.setEnabled(false);
        btnGuardar.setVisibility(View.GONE);
        btnEliminar.setVisibility(View.GONE);
    }
    // Guarda los cambios o crea un nuevo usuario
    private void guardarUsuario() {
        // Validacion basica
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
        // Actualiza los datos del usuario
        usuarioActual.setNombre(etNombre.getText().toString());
        usuarioActual.setApellido(etApellido.getText().toString());
        usuarioActual.setCorreo(etCorreo.getText().toString());
        String contrasena = etContrasena.getText().toString();
        if (!TextUtils.isEmpty(contrasena)) {
            usuarioActual.setContrasena(contrasena);
        }
        // Asigna el rol
        usuarioActual.setRol(spinnerRol.getSelectedItemPosition() == 0 ? Rol.ADMIN : Rol.GESTOR);
        // Preserva la fecha de creacion si está editando
        if (modoEdicion && usuarioActual.getFechaCreacion() != null) {
            usuarioActual.setFechaCreacion(usuarioActual.getFechaCreacion());
        }
        // Muestra el progreso
        showProgress(true);
        // Llama al servicio adecuado: actualizar o crear
        Call<Usuario> call;
        if (modoEdicion) {
            call = usuarioService.actualizarUsuario(usuarioActual.getId(), usuarioActual);
        } else {
            call = usuarioService.crearUsuario(usuarioActual);
        }
        // Ejecuta la llamada a la API
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Usuario guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad
                } else {
                    Toast.makeText(UsuarioDetalleActivity.this,
                            "Error al guardar usuario: " + response.message(), Toast.LENGTH_SHORT).show();
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
    // Muestra diálogo de confirmacion antes de eliminar
    private void confirmarEliminarUsuario() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Está seguro que desea eliminar este usuario?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarUsuario())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    // Elimina el usuario actual
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
                            "Error al eliminar usuario: " + response.message(), Toast.LENGTH_SHORT).show();
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
    // Muestra u oculta el ProgressBar y desactiva botones
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!show);
        btnEliminar.setEnabled(!show);
    }
    // Maneja el boton de retroceso en el toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Vuelve a la pantalla anterior
        return true;
    }
}

