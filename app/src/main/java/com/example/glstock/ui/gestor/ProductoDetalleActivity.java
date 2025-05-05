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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoDetalleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnEliminar;
    private Button btnGuardar;
    private Button btnNuevaCategoria;
    private Spinner spinnerCategoria;
    private TextInputEditText etNombre;
    private TextInputEditText etDescripcion;
    private TextInputEditText etPrecio;
    private TextInputEditText etCantidad;
    private TextInputEditText etUrlImagen;
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

        // Inicializar vistas
        inicializarVistas();

        if (getIntent().hasExtra("producto_objeto")) {
            modoEdicion = true;
            productoActual = (Producto) getIntent().getSerializableExtra("producto_objeto");
            if (productoActual != null) {
                mostrarDatosProducto();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Editar Producto");
                }
            }
        }

        // Configurar toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        // Verificar si estamos en modo edición (recibiendo un producto existente)
        if (getIntent().hasExtra("producto_id")) {
            modoEdicion = true;
            long productoId = getIntent().getLongExtra("producto_id", -1);
            if (productoId != -1) {
                cargarProducto(productoId);
                getSupportActionBar().setTitle("Editar Producto");
            }
        } else {
            modoEdicion = false;
            productoActual = new Producto();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.sql.Date fechaActual = new java.sql.Date(calendar.getTimeInMillis());
            productoActual.setFechaIngreso(fechaActual);
            getSupportActionBar().setTitle("Nuevo Producto");
            btnEliminar.setVisibility(View.GONE); // Ocultar botón eliminar en modo creación
        }

        // Cargar categorías para el spinner
        cargarCategorias();

        // Configurar botones
        btnGuardar.setOnClickListener(v -> guardarProducto());
        btnEliminar.setOnClickListener(v -> confirmarEliminarProducto());

        // Agregar botón para crear categoría
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NUEVA_CATEGORIA && resultCode == RESULT_OK) {
            // Actualizar lista de categorías
            cargarCategorias();
            Toast.makeText(this, "Categoría creada con éxito", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarProducto(long productoId) {
        showProgress(true);
        Call<Producto> call = productoService.buscarPorId(productoId);
        call.enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    productoActual = response.body();
                    mostrarDatosProducto();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Error al cargar producto", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
                    // Si estamos en modo edición, seleccionar la categoría del producto
                    if (modoEdicion && productoActual != null && productoActual.getCategoria() != null) {
                        seleccionarCategoria(productoActual.getCategoria().getId());
                    }
                } else {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerCategorias() {
        List<String> nombresCategorias = new ArrayList<>();
        for (Categoria categoria : categorias) {
            nombresCategorias.add(categoria.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombresCategorias);
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

        // Cargar imagen si existe URL
        if (!TextUtils.isEmpty(productoActual.getUrlImagen())) {
            Glide.with(this)
                    .load(productoActual.getUrlImagen())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivProducto);
        }
    }

    private void guardarProducto() {
        // Validar campos obligatorios
        if (TextUtils.isEmpty(etNombre.getText())) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }

        if (TextUtils.isEmpty(etPrecio.getText())) {
            etPrecio.setError("El precio es obligatorio");
            return;
        }

        if (TextUtils.isEmpty(etCantidad.getText())) {
            etCantidad.setError("La cantidad es obligatoria");
            return;
        }

        // Obtener categoría seleccionada
        int posicionCategoria = spinnerCategoria.getSelectedItemPosition();
        if (posicionCategoria < 0 || posicionCategoria >= categorias.size()) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return;
        }
        Categoria categoriaSeleccionada = categorias.get(posicionCategoria);

        // Actualizar datos del producto
        productoActual.setNombre(etNombre.getText().toString());
        productoActual.setDescripcion(etDescripcion.getText().toString());
        productoActual.setCategoria(categoriaSeleccionada);
        productoActual.setPrecio(Double.parseDouble(etPrecio.getText().toString()));
        productoActual.setCantidad(Integer.parseInt(etCantidad.getText().toString()));
        productoActual.setUrlImagen(etUrlImagen.getText().toString());

        showProgress(true);

        // Guardar producto (crear o actualizar)
        Call<Producto> call;
        if (modoEdicion && productoActual.getId() != null) {
            // Verificar que se está enviando el ID
            Long idProducto = productoActual.getId();
            System.out.println("Actualizando producto con ID: " + idProducto);
            call = productoService.actualizarProducto(idProducto, productoActual);
        } else {
            call = productoService.crearProducto(productoActual);
        }

        call.enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Error al guardar producto: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarEliminarProducto() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Está seguro que desea eliminar este producto?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto() {
        if (productoActual == null || productoActual.getId() == null) {
            Toast.makeText(this, "No se puede eliminar el producto", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        productoService.eliminarProducto(productoActual.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Producto eliminado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this,
                            "Error al eliminar producto: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductoDetalleActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonsContainer.setEnabled(!show);
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