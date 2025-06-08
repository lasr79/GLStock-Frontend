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
import androidx.cardview.widget.CardView;

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

// Actividad para crear, ver o editar un producto
public class ProductoDetalleActivity extends AppCompatActivity {
    // Elementos de UI
    private Toolbar toolbar;
    private Button btnEliminar, btnGuardar, btnNuevaCategoria;
    private Spinner spinnerCategoria;
    private TextInputEditText etNombre, etDescripcion, etPrecio, etCantidad, etUrlImagen;
    private ImageView ivProducto;
    private ProgressBar progressBar;
    private LinearLayout buttonsContainer;
    // Servicios de API
    private ProductoService productoService;
    private CategoriaService categoriaService;
    // Datos en memoria
    private List<Categoria> categorias = new ArrayList<>();
    private Producto productoActual;
    private boolean modoEdicion = false;
    // Codigo para identificar resultado al volver de agregar nueva categoria
    private static final int REQUEST_NUEVA_CATEGORIA = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);
        // Vincula las vistas
        inicializarVistas();
        // Configura el toolbar
        setSupportActionBar(toolbar);
        CardView cardImagen = findViewById(R.id.cardImagenProducto);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita la flecha atrás
        }
        // Inicializa los servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);
        // Obtiene el producto si fue pasado como parametro
        productoActual = (Producto) getIntent().getSerializableExtra("producto_objeto");
        // Verifica si el usuario es admin para habilitar edicion
        boolean esAdmin = SessionManager.getInstance().isAdmin();
        // Si se recibio un producto (modo edicion o visualizacion)
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
                desactivarCampos(); // Solo visualización
            }
        } else {
            // Modo nuevo producto
            productoActual = new Producto();
            productoActual.setFechaIngreso(new java.sql.Date(System.currentTimeMillis()));
            getSupportActionBar().setTitle("Nuevo Producto");
            cardImagen.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            modoEdicion = false;
        }
        // Carga las categorias desde la API
        cargarCategorias();
        // Listeners de botones
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
    // Llama a la API para obtener todas las categorias
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
    // Llena el spinner con los nombres de las categorias
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
    // Selecciona una categoria por su id en el spinner
    private void seleccionarCategoria(Long categoriaId) {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getId().equals(categoriaId)) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }
    }
    // Muestra los datos del producto actual en los campos
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
    // Valida los campos y llama a la API para guardar o actualizar un producto
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
        // Asigna los valores ingresados al producto
        productoActual.setNombre(etNombre.getText().toString());
        productoActual.setDescripcion(etDescripcion.getText().toString());
        productoActual.setPrecio(Double.parseDouble(etPrecio.getText().toString()));
        productoActual.setCantidad(Integer.parseInt(etCantidad.getText().toString()));
        productoActual.setUrlImagen(etUrlImagen.getText().toString());
        productoActual.setCategoria(categorias.get(pos));
        showProgress(true);
        // Decide si se va a actualizar o crear un nuevo producto
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
    // Muestra un cuadro de confirmacion antes de eliminar
    private void confirmarEliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Seguro que deseas eliminarlo?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    // Llama a la API para eliminar el producto
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
    // Desactiva todos los campos cuando se esta en modo solo lectura
    private void desactivarCampos() {
        etNombre.setEnabled(false);
        etDescripcion.setEnabled(false);
        etPrecio.setEnabled(false);
        etCantidad.setEnabled(false);
        etUrlImagen.setEnabled(false);
        spinnerCategoria.setEnabled(false);
        btnNuevaCategoria.setVisibility(View.GONE);
    }
    // Muestra u oculta el ProgressBar y desactiva inputs
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonsContainer.setEnabled(!show);
    }
    // Maneja el boton de volver en el Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Vuelve a la pantalla anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}