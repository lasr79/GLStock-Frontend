package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.glstock.R;
import com.example.glstock.adapter.ProductoCardAdapter;
import com.example.glstock.adapter.ProductoCategoriasPagerAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.ActivityProductosTabsBinding;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Producto;
import com.example.glstock.ui.MainActivity;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosTabsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityProductosTabsBinding binding;
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private ProductoCategoriasPagerAdapter pagerAdapter;
    private List<Categoria> categorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductosTabsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inicializar servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        // Configurar BottomNavigationView
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Mostrar u ocultar la sección de usuarios según el rol
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }

        // Configurar botón de búsqueda
        binding.btnBuscar.setOnClickListener(v -> buscarProductos());

        // Mostrar el botón flotante solo si el usuario es ADMIN
        if (SessionManager.getInstance().isAdmin()) {
            binding.fabAddProduct.setVisibility(View.VISIBLE);
            binding.fabAddProduct.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductoDetalleActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fabAddProduct.setVisibility(View.GONE);
        }

        // Configurar chips de filtrado
        configurarChips();

        // Cargar categorías para las pestañas
        cargarCategorias();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar productos al regresar a la actividad
        if (binding.viewPager.getCurrentItem() == 0) {
            // Si estamos en la pestaña "Todos", actualizar los productos
            CategoriaProductosFragment fragmentoActual = (CategoriaProductosFragment)
                    getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
            if (fragmentoActual != null) {
                fragmentoActual.cargarProductos();
            }
        }
    }

    private void configurarChips() {
        binding.chipTodos.setOnClickListener(v -> {
            // Filtro general (opcional)
        });

        binding.chipRecientes.setOnClickListener(v -> cargarProductosRecientes());

        binding.chipBajoStock.setOnClickListener(v -> cargarProductosBajoStock());

        binding.chipPrecio.setOnClickListener(v -> {
            Chip chip = (Chip) v;
            if (chip.getText().toString().contains("↑")) {
                chip.setText("Precio ↓");
                ordenarProductosPorPrecioDescendente();
            } else {
                chip.setText("Precio ↑");
                ordenarProductosPorPrecioAscendente();
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
                    configurarViewPagerConCategorias();
                } else {
                    Toast.makeText(ProductosTabsActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarViewPagerConCategorias() {
        pagerAdapter = new ProductoCategoriasPagerAdapter(this, categorias, true);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Todos");
            } else {
                tab.setText(categorias.get(position - 1).getNombre());
            }
        }).attach();
    }

    private void buscarProductos() {
        String query = binding.etBuscar.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        productoService.buscarPorNombre(query).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(ProductosTabsActivity.this, "No se encontraron productos", Toast.LENGTH_SHORT).show();
                    } else {
                        binding.viewPager.setCurrentItem(0);
                        actualizarFragmentoActual(response.body());
                    }
                } else {
                    Toast.makeText(ProductosTabsActivity.this, "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    binding.viewPager.setCurrentItem(0);
                    actualizarFragmentoActual(response.body());
                    Toast.makeText(ProductosTabsActivity.this, "Mostrando productos recientes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductosTabsActivity.this, "Error al cargar productos recientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProductosBajoStock() {
        showProgress(true);
        productoService.obtenerProductosConMenorStock().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    binding.viewPager.setCurrentItem(0);
                    actualizarFragmentoActual(response.body());
                    Toast.makeText(ProductosTabsActivity.this, "Mostrando productos con bajo stock", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductosTabsActivity.this, "Error al cargar productos con bajo stock", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ordenarProductosPorPrecioAscendente() {
        CategoriaProductosFragment fragmentoActual = (CategoriaProductosFragment)
                getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());

        if (fragmentoActual != null) {
            fragmentoActual.ordenarPorPrecioAscendente();
            Toast.makeText(this, "Productos ordenados por precio ↑", Toast.LENGTH_SHORT).show();
        }
    }

    private void ordenarProductosPorPrecioDescendente() {
        CategoriaProductosFragment fragmentoActual = (CategoriaProductosFragment)
                getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());

        if (fragmentoActual != null) {
            fragmentoActual.ordenarPorPrecioDescendente();
            Toast.makeText(this, "Productos ordenados por precio ↓", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarFragmentoActual(List<Producto> productos) {
        CategoriaProductosFragment fragmentoActual = (CategoriaProductosFragment)
                getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());

        if (fragmentoActual != null) {
            fragmentoActual.actualizarProductos(productos);
        }
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();

            // Importante: finalizar la actividad actual antes de iniciar la nueva
            if (itemId == R.id.navigation_inicio) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish(); // Añadir esto
                return true;
            } else if (itemId == R.id.navigation_reportes) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "reportes");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish(); // Añadir esto
                return true;
            } else if (itemId == R.id.navigation_usuarios) {
                if (SessionManager.getInstance().isAdmin()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("fragment", "usuarios");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish(); // Añadir esto
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // Registrar el error para diagnóstico
            e.printStackTrace();
            Toast.makeText(this, "Error de navegación: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}