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

public class CategoriaDetalleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etNombre;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private CategoriaService categoriaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria_detalle);

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        etNombre = findViewById(R.id.etNombre);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nueva Categoría");
        }

        // Inicializar servicio
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        // Configurar botón
        btnGuardar.setOnClickListener(v -> guardarCategoria());
    }

    private void guardarCategoria() {
        // Validar campo obligatorio
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        showProgress(true);

        // Crear objeto Categoría
        Categoria categoria = new Categoria();
        categoria.setNombre(etNombre.getText().toString());

        // Si tienes un campo para IVA, puedes configurarlo aquí
        Iva iva = new Iva();
        iva.setId(1L); // ID por defecto, ajusta según tu estructura
        categoria.setIva(iva);

        // Guardar categoría
        categoriaService.crearCategoria(categoria).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
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

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}