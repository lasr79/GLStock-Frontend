package com.example.glstock.ui.gestor;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
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

        // Inicializar servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);

        // Configurar BottomNavigationView
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Configurar botón de búsqueda
        binding.btnBuscar.setOnClickListener(v -> buscarProductos());

        // Configurar chips de filtrado
        configurarChips();

        // Cargar categorías para las pestañas
        cargarCategorias();
    }

    private void configurarChips() {
        binding.chipTodos.setOnClickListener(v -> {
            // Este filtro se aplica desde la pestaña "Todos"
            // Podríamos recargar los productos sin filtros aquí
        });

        binding.chipRecientes.setOnClickListener(v -> {
            // Cargar productos recientes
            cargarProductosRecientes();
        });

        binding.chipBajoStock.setOnClickListener(v -> {
            // Cargar productos con bajo stock
            cargarProductosBajoStock();
        });

        binding.chipPrecio.setOnClickListener(v -> {
            Chip chip = (Chip) v;
            if (chip.getText().toString().contains("↑")) {
                chip.setText("Precio ↓");
                // Ordenar por precio descendente
                ordenarProductosPorPrecioDescendente();
            } else {
                chip.setText("Precio ↑");
                // Ordenar por precio ascendente
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
                    Toast.makeText(ProductosTabsActivity.this,
                            "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarViewPagerConCategorias() {
        pagerAdapter = new ProductoCategoriasPagerAdapter(this, categorias, true);
        binding.viewPager.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
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
                    // Aquí deberíamos mostrar los resultados
                    // Podríamos crear un nuevo fragmento o actualizar el actual
                    if (response.body().isEmpty()) {
                        Toast.makeText(ProductosTabsActivity.this,
                                "No se encontraron productos con ese nombre",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Navegar a pestaña "Todos" y actualizar con resultados
                        binding.viewPager.setCurrentItem(0);
                        // Idealmente deberíamos tener un método para actualizar
                        // los productos en el fragmento actual
                    }
                } else {
                    Toast.makeText(ProductosTabsActivity.this,
                            "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    // Ir a la pestaña "Todos" y actualizar los datos
                    binding.viewPager.setCurrentItem(0);
                    actualizarFragmentoActual(response.body());
                    Toast.makeText(ProductosTabsActivity.this,
                            "Mostrando productos recientes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductosTabsActivity.this,
                            "Error al cargar productos recientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    // Ir a la pestaña "Todos" y actualizar los datos
                    binding.viewPager.setCurrentItem(0);
                    actualizarFragmentoActual(response.body());
                    Toast.makeText(ProductosTabsActivity.this,
                            "Mostrando productos con bajo stock", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductosTabsActivity.this,
                            "Error al cargar productos con bajo stock", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProductosTabsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ordenarProductosPorPrecioAscendente() {
        // Obtener el fragmento actual y ordenar sus productos
        CategoriaProductosFragment fragmentoActual =
                (CategoriaProductosFragment) getSupportFragmentManager()
                        .findFragmentByTag("f" + binding.viewPager.getCurrentItem());

        if (fragmentoActual != null) {
            fragmentoActual.ordenarPorPrecioAscendente();
            Toast.makeText(this, "Productos ordenados por precio ascendente", Toast.LENGTH_SHORT).show();
        }
    }

    private void ordenarProductosPorPrecioDescendente() {
        // Obtener el fragmento actual y ordenar sus productos
        CategoriaProductosFragment fragmentoActual =
                (CategoriaProductosFragment) getSupportFragmentManager()
                        .findFragmentByTag("f" + binding.viewPager.getCurrentItem());

        if (fragmentoActual != null) {
            fragmentoActual.ordenarPorPrecioDescendente();
            Toast.makeText(this, "Productos ordenados por precio descendente", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarFragmentoActual(List<Producto> productos) {
        // Obtener el fragmento actual y actualizar sus productos
        CategoriaProductosFragment fragmentoActual =
                (CategoriaProductosFragment) getSupportFragmentManager()
                        .findFragmentByTag("f" + binding.viewPager.getCurrentItem());

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
        // Manejar navegación del bottom menu
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_inicio) {
            finish(); // Volver a inicio
            return true;
        } else if (itemId == R.id.navigation_reportes) {
            // Ir a reportes
            // Intent intent = new Intent(this, ReportesActivity.class);
            // startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_usuarios) {
            // Ir a usuarios (solo admin)
            // Intent intent = new Intent(this, UsuariosActivity.class);
            // startActivity(intent);
            return true;
        }

        return false;
    }
}