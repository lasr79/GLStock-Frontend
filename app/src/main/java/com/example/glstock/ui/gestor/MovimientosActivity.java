package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.api.MovimientoService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.ActivityMovimientosBinding;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Movimiento;
import com.example.glstock.model.Producto;
import com.example.glstock.model.TipoMovimiento;
import com.example.glstock.ui.MainActivity;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Actividad para registrar movimientos (entrada/salida)
public class MovimientosActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    // ViewBinding para acceder a las vistas
    private ActivityMovimientosBinding binding;
    // Servicios para consumir la API
    private ProductoService productoService;
    private MovimientoService movimientoService;
    private CategoriaService categoriaService;
    // Datos en memoria
    private List<Producto> listaProductos;
    private List<Categoria> listaCategorias;
    private Producto productoSeleccionado;
    private Categoria categoriaSeleccionada;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovimientosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configuracion del Toolbar
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Flecha atrás
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Registrar Movimiento");
            }
        }
        // Configura la barra de navegación inferior
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
            // Oculta seccion de usuarios si no es admin
            MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
            if (usuariosItem != null) {
                usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
            }
        }
        // Inicializa servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        movimientoService = ApiClient.getClient().create(MovimientoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);
        // Carga categorias al iniciar
        cargarCategorias();
        // Listener para seleccionar categoria
        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    categoriaSeleccionada = null;
                    cargarTodosLosProductos();
                } else {
                    categoriaSeleccionada = listaCategorias.get(position - 1);
                    cargarProductosPorCategoria(categoriaSeleccionada.getId());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                categoriaSeleccionada = null;
            }
        });
        // Listener para seleccionar producto
        binding.spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaProductos != null && !listaProductos.isEmpty() && position < listaProductos.size()) {
                    productoSeleccionado = listaProductos.get(position);
                    actualizarDetallesProducto();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productoSeleccionado = null;
                binding.tvStockActual.setText("Stock actual: N/A");
            }
        });
        binding.rgTipoMovimiento.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esSalida = (checkedId == R.id.rbSalida);
            binding.etCantidad.setHint(esSalida ? "Cantidad a retirar" : "Cantidad a ingresar");
            binding.etMotivo.setHint(esSalida ? "Motivo de salida" : "Motivo de entrada");
            actualizarDetallesProducto();
        });
        // Boton registrar movimiento
        binding.btnRegistrar.setOnClickListener(v -> registrarMovimiento());
    }
    // Carga las categorias desde el backend
    private void cargarCategorias() {
        showProgress(true);
        categoriaService.listarCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCategorias = response.body();
                    configurarSpinnerCategorias();
                    cargarTodosLosProductos();
                } else {
                    showProgress(false);
                    Toast.makeText(MovimientosActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Llena el spinner de categorias
    private void configurarSpinnerCategorias() {
        List<String> nombresCategorias = new ArrayList<>();
        nombresCategorias.add("Todas las categorías");
        for (Categoria categoria : listaCategorias) {
            nombresCategorias.add(categoria.getNombre());
        }
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombresCategorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapterCategorias);
    }
    // Carga todos los productos recientes
    private void cargarTodosLosProductos() {
        productoService.productosRecientes().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaProductos = response.body();
                    configurarSpinnerProductos();
                } else {
                    Toast.makeText(MovimientosActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Carga productos filtrados por categoria
    private void cargarProductosPorCategoria(Long categoriaId) {
        showProgress(true);
        Map<String, Long> datos = new HashMap<>();
        datos.put("idCategoria", categoriaId);
        productoService.buscarPorCategoria(datos).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaProductos = response.body();
                    configurarSpinnerProductos();

                    if (listaProductos.isEmpty()) {
                        Toast.makeText(MovimientosActivity.this, "No hay productos en esta categoría", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MovimientosActivity.this, "Error al cargar productos de la categoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Llena el spinner de productos
    private void configurarSpinnerProductos() {
        List<String> nombresProductos = new ArrayList<>();
        for (Producto producto : listaProductos) {
            nombresProductos.add(producto.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombresProductos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProducto.setAdapter(adapter);

        if (!nombresProductos.isEmpty()) {
            binding.spinnerProducto.setSelection(0);
        }
    }
    // Muestra el stock del producto seleccionado y limite de retiro
    private void actualizarDetallesProducto() {
        if (productoSeleccionado != null) {
            binding.tvStockActual.setText("Stock actual: " + productoSeleccionado.getCantidad());
            if (binding.rbSalida.isChecked()) {
                binding.tvLimiteRetiro.setVisibility(View.VISIBLE);
                binding.tvLimiteRetiro.setText("Límite de retiro: " + productoSeleccionado.getCantidad());
            } else {
                binding.tvLimiteRetiro.setVisibility(View.GONE);
            }
        } else {
            binding.tvStockActual.setText("Stock actual: N/A");
            binding.tvLimiteRetiro.setVisibility(View.GONE);
        }
    }
    // Logica del registro del movimiento
    private void registrarMovimiento() {
        if (productoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un producto", Toast.LENGTH_SHORT).show();
            return;
        }
        String cantidadStr = binding.etCantidad.getText().toString().trim();
        if (TextUtils.isEmpty(cantidadStr)) {
            binding.etCantidad.setError("Ingrese una cantidad");
            return;
        }
        String motivo = binding.etMotivo.getText().toString().trim();
        if (TextUtils.isEmpty(motivo)) {
            binding.etMotivo.setError("Ingrese un motivo");
            return;
        }
        int cantidad = Integer.parseInt(cantidadStr);
        if (cantidad <= 0) {
            binding.etCantidad.setError("La cantidad debe ser mayor que 0");
            return;
        }
        if (binding.rbSalida.isChecked() && cantidad > productoSeleccionado.getCantidad()) {
            binding.etCantidad.setError("La cantidad excede el stock disponible");
            return;
        }
        TipoMovimiento tipoMovimiento = binding.rbEntrada.isChecked() ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;
        showProgress(true);
        // Solo se necesita el ID del producto
        Producto productoSoloId = new Producto();
        productoSoloId.setId(productoSeleccionado.getId());
        Movimiento movimiento = new Movimiento();
        movimiento.setProducto(productoSoloId);
        movimiento.setTipo(tipoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        // Registrar el movimiento
        movimientoService.registrarMovimiento(movimiento).enqueue(new Callback<Movimiento>() {
            @Override
            public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MovimientosActivity.this, "Movimiento registrado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MovimientosActivity.this, "Error al registrar movimiento: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Movimiento> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Muestra u oculta el progreso y bloquea inputs
    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegistrar.setEnabled(!show);
        binding.spinnerProducto.setEnabled(!show);
        binding.spinnerCategoria.setEnabled(!show);
        binding.etCantidad.setEnabled(!show);
        binding.etMotivo.setEnabled(!show);
        binding.rgTipoMovimiento.setEnabled(!show);
    }
    // Accion del botón de volver en toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Maneja la navegacion inferior
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_inicio) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_reportes) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "reportes");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_usuarios && SessionManager.getInstance().isAdmin()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "usuarios");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "Error de navegación: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}