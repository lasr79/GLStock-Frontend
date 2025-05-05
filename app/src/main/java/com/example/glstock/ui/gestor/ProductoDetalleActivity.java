package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Producto;
import com.example.glstock.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoDetalleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnEliminar, btnGuardar, btnNuevaCategoria;
    private Spinner spinnerCategoria;
    private TextInputEditText etNombre, etDescripcion, etPrecio, etCantidad, etUrlImagen;
    private ImageView ivProducto;
    private ProgressBar progressBar;
    private LinearLayout buttonsContainer;

    private ProductoService productoService;
    private CategoriaService categoriaService;
    private List<Categoria> categorias = new ArrayList<>();
    private Producto productoActual;
    private boolean modoEdicion = false;

    private static final int REQUEST_NUEVA_CATEGORIA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        inicializarVistas();
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        productoActual = (Producto) getIntent().getSerializableExtra("producto_objeto");

        boolean esAdmin = SessionManager.getInstance().isAdmin();

        if (productoActual != null) {
            mostrarDatosProducto();
            if (esAdmin) {
                getSupportActionBar().setTitle("Editar Producto");
                btnGuardar.setVisibility(View.VISIBLE);
                btnEliminar.setVisibility(View.VISIBLE);
                modoEdicion = true;
            } else {
                getSupportActionBar().setTitle("Detalle Producto");
                btnGuardar.setVisibility(View.GONE);
                btnEliminar.setVisibility(View.GONE);
                desactivarCampos();
            }
        } else {
            productoActual = new Producto();
            productoActual.setFechaIngreso(new java.sql.Date(System.currentTimeMillis()));
            getSupportActionBar().setTitle("Nuevo Producto");
            btnEliminar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            modoEdicion = false;
        }

        cargarCategorias();

        btnGuardar.setOnClickListener(v -> guardarProducto());
        btnEliminar.setOnClickListener(v -> confirmarEliminarProducto());
        btnNuevaCategoria.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoriaDetalleActivity.class);
            startActivityForResult(intent, REQUEST_NUEVA_CATEGORIA);
        });
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnNuevaCategoria = findViewById(R.id.btnNuevaCategoria);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etCantidad = findViewById(R.id.etCantidad);
        etUrlImagen = findViewById(R.id.etUrlImagen);
        ivProducto = findViewById(R.id.ivProducto);
        progressBar = findViewById(R.id.progressBar);
        buttonsContainer = findViewById(R.id.buttonsContainer);
    }

    private void cargarCategorias() {
        showProgress(true);
        categoriaService.listarCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    categorias = response.body();
                    configurarSpinnerCategorias();
                    if (modoEdicion && productoActual.getCategoria() != null) {
                        seleccionarCategoria(productoActual.getCategoria().getId());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerCategorias() {
        List<String> nombres = new ArrayList<>();
        for (Categoria c : categorias) {
            nombres.add(c.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void seleccionarCategoria(Long categoriaId) {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getId().equals(categoriaId)) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }
    }

    private void mostrarDatosProducto() {
        etNombre.setText(productoActual.getNombre());
        etDescripcion.setText(productoActual.getDescripcion());
        etPrecio.setText(String.valueOf(productoActual.getPrecio()));
        etCantidad.setText(String.valueOf(productoActual.getCantidad()));
        etUrlImagen.setText(productoActual.getUrlImagen());

        if (!TextUtils.isEmpty(productoActual.getUrlImagen())) {
            Glide.with(this)
                    .load(productoActual.getUrlImagen())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivProducto);
        }
    }

    private void guardarProducto() {
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("Campo obligatorio");
            return;
        }

        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos < 0 || pos >= categorias.size()) {
            Toast.makeText(this, "Seleccione una categoría válida", Toast.LENGTH_SHORT).show();
            return;
        }

        productoActual.setNombre(etNombre.getText().toString());
        productoActual.setDescripcion(etDescripcion.getText().toString());
        productoActual.setPrecio(Double.parseDouble(etPrecio.getText().toString()));
        productoActual.setCantidad(Integer.parseInt(etCantidad.getText().toString()));
        productoActual.setUrlImagen(etUrlImagen.getText().toString());
        productoActual.setCategoria(categorias.get(pos));

        showProgress(true);

        Call<Producto> call = (modoEdicion && productoActual.getId() != null)
                ? productoService.actualizarProducto(productoActual.getId(), productoActual)
                : productoService.crearProducto(productoActual);

        call.enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Seguro que deseas eliminarlo?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto() {
        if (productoActual.getId() == null) return;

        showProgress(true);
        productoService.eliminarProducto(productoActual.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this, "Eliminado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void desactivarCampos() {
        etNombre.setEnabled(false);
        etDescripcion.setEnabled(false);
        etPrecio.setEnabled(false);
        etCantidad.setEnabled(false);
        etUrlImagen.setEnabled(false);
        spinnerCategoria.setEnabled(false);
        btnNuevaCategoria.setVisibility(View.GONE);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonsContainer.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}