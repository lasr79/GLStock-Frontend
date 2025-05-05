package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.glstock.adapter.ProductoAdapter;
import com.example.glstock.adapter.ProductoCardAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.ActivityProductosBinding;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Producto;
import com.example.glstock.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosActivity extends AppCompatActivity implements ProductoCardAdapter.OnProductoClickListener {

    private ActivityProductosBinding binding;
    private ProductoCardAdapter adapter;
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Productos");

        progressBar = binding.progressBar;

        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        adapter = new ProductoCardAdapter(this); // USAMOS el CardAdapter
        binding.rvProductos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProductos.setAdapter(adapter);

        binding.btnBuscar.setOnClickListener(v -> {
            String query = binding.etBuscar.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                buscarProductos();
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });

        cargarProductosRecientes();

        binding.rgFiltro.setOnCheckedChangeListener((group, checkedId) -> {
            binding.etBuscar.setHint(
                    checkedId == binding.rbProducto.getId() ?
                            "Buscar por nombre de producto..." :
                            "Buscar por nombre de categoría..."
            );
        });
    }

    private void buscarProductos() {
        String query = binding.etBuscar.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            cargarProductosRecientes();
            return;
        }

        showProgress(true);

        if (binding.rbProducto.isChecked()) {
            productoService.buscarPorNombre(query).enqueue(new Callback<List<Producto>>() {
                @Override
                public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                    showProgress(false);
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.setProductos(response.body());
                        if (response.body().isEmpty()) {
                            mostrarMensajeNoResultados("No se encontraron productos con ese nombre");
                        }
                    } else {
                        Toast.makeText(ProductosActivity.this, "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Producto>> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(ProductosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            categoriaService.listarCategorias().enqueue(new Callback<List<Categoria>>() {
                @Override
                public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean encontrada = false;
                        for (Categoria categoria : response.body()) {
                            if (categoria.getNombre().toLowerCase().contains(query.toLowerCase())) {
                                buscarProductosPorCategoria(categoria);
                                encontrada = true;
                                break;
                            }
                        }
                        if (!encontrada) {
                            showProgress(false);
                            mostrarMensajeNoResultados("No se encontró la categoría especificada");
                        }
                    } else {
                        showProgress(false);
                        Toast.makeText(ProductosActivity.this, "Error al buscar categorías", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Categoria>> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(ProductosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void buscarProductosPorCategoria(Categoria categoria) {
        Map<String, Long> datos = new HashMap<>();
        datos.put("idCategoria", categoria.getId());

        productoService.buscarPorCategoria(datos).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProductos(response.body());
                    if (response.body().isEmpty()) {
                        mostrarMensajeNoResultados("No hay productos en esta categoría");
                    }
                } else {
                    Toast.makeText(ProductosActivity.this, "Error al buscar productos por categoría", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProductosRecientes() {
        showProgress(true);
        productoService.productosRecientes().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProductos(response.body());
                } else {
                    Toast.makeText(ProductosActivity.this, "Error al cargar productos recientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMensajeNoResultados(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProductoClick(Producto producto) {
        Log.d("DEBUG_PRODUCTO", "ID: " + producto.getId() + ", Nombre: " + producto.getNombre());

        if (producto.getId() != null) {
            Intent intent = new Intent(ProductosActivity.this, ProductoDetalleActivity.class);
            intent.putExtra("producto_id", producto.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Producto sin ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}