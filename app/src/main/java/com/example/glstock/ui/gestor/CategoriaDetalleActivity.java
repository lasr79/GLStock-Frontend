package com.example.glstock.ui.gestor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Iva;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Actividad para crear una nueva categoria
public class CategoriaDetalleActivity extends AppCompatActivity {
    // Declaración de vistas
    private Toolbar toolbar;
    private TextInputEditText etNombre;
    private Button btnGuardar;
    private ProgressBar progressBar;
    private CategoriaService categoriaService;
    // Metodo que se ejecuta al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria_detalle); // Carga el layout XML
        // Inicializa las vistas del layout
        toolbar = findViewById(R.id.toolbar);
        etNombre = findViewById(R.id.etNombre);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);
        // Configura el toolbar como ActionBar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Muestra el boton de volver
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Establece el titulo de la barra
            getSupportActionBar().setTitle("Nueva Categoría");
        }
        // Inicializa API
        categoriaService = ApiClient.getClient().create(CategoriaService.class);
        // Configura el boton de guardar para ejecutar el metodo guardarCategoria()
        btnGuardar.setOnClickListener(v -> guardarCategoria());
    }
    // Metodo para guardar la categoria
    private void guardarCategoria() {
        // Verifica que el campo nombre no esté vacío
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }
        // Muestra el indicador de carga
        showProgress(true);
        // Crea un nuevo objeto Categoria y le asigna el nombre ingresado
        Categoria categoria = new Categoria();
        categoria.setNombre(etNombre.getText().toString());
        // Crea un objeto iva y le asigna un ID por defecto
        Iva iva = new Iva();
        iva.setId(1L);
        categoria.setIva(iva);
        // Llama al servicio para guardar la categoria
        categoriaService.crearCategoria(categoria).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                // Oculta el indicador de carga
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Si fue exitoso, muestra mensaje y cierra la actividad
                    Toast.makeText(CategoriaDetalleActivity.this,
                            "Categoría guardada con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CategoriaDetalleActivity.this,
                            "Error al guardar categoría: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                showProgress(false);
                Toast.makeText(CategoriaDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Metodo para mostrar u ocultar el ProgressBar
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE); // Cambia visibilidad
        btnGuardar.setEnabled(!show); // Desactiva el botón mientras se guarda
    }
    // Maneja el evento cuando se toca el boton de retroceso en la toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Si el ítem seleccionado es el botón de volver
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
