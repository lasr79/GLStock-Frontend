package com.example.glstock.ui.gestor;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.glstock.R;
import com.example.glstock.adapter.ProductoCardAdapter;
import com.example.glstock.adapter.ProductoCategoriasPagerAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.api.ReporteService;
import com.example.glstock.databinding.ActivityProductosTabsBinding;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Producto;
import com.example.glstock.ui.MainActivity;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Actividad principal que muestra productos con generación de PDF sin solicitar permisos
public class ProductosTabsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityProductosTabsBinding binding;
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private ReporteService reporteService;
    private ProductoCategoriasPagerAdapter pagerAdapter;
    private CategoriaProductosFragment consultasFragment;
    private List<Categoria> categorias = new ArrayList<>();
    private String filtroActivo = "consultas";
    private String categoriaDesdeIntent = null;
    private boolean desdeReporte = false;
    private boolean ignorarCambioPestaña = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductosTabsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarToolbar();
        inicializarServicios();
        configurarBottomNavigation();
        configurarAcciones();

        String modo = getIntent().getStringExtra("modo_reporte");
        boolean filtrarInmediatamente = getIntent().getBooleanExtra("filtrar_inmediatamente", false);
        categoriaDesdeIntent = getIntent().getStringExtra("categoria");

        // CORREGIDO - Mejorar la lógica de detección
        desdeReporte = (modo != null) || filtrarInmediatamente;
        ignorarCambioPestaña = desdeReporte;

        // DEBUG - Log para verificar parámetros
        android.util.Log.d("ProductosTabsActivity", "modo: " + modo + ", filtrarInmediatamente: " + filtrarInmediatamente + ", categoria: " + categoriaDesdeIntent);

        cargarCategorias(modo, categoriaDesdeIntent, filtrarInmediatamente);
    }

    private void configurarToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void inicializarServicios() {
        productoService = ApiClient.getClient().create(ProductoService.class);
        categoriaService = ApiClient.getClient().create(CategoriaService.class);
        reporteService = ApiClient.getClient().create(ReporteService.class);
    }

    private void configurarBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }
    }

    private void configurarAcciones() {
        binding.btnBuscar.setOnClickListener(v -> buscarProductos());

        if (SessionManager.getInstance().isAdmin()) {
            binding.fabAddProduct.setVisibility(View.VISIBLE);
            binding.fabAddProduct.setOnClickListener(v -> startActivity(new Intent(this, ProductoDetalleActivity.class)));
        } else {
            binding.fabAddProduct.setVisibility(View.GONE);
        }

        binding.fabGenerarPdf.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("¿Descargar el reporte en PDF?")
                    .setPositiveButton("Descargar", (dialog, which) -> generarPdfSegunFiltro())
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        configurarChips();
    }

    private void configurarChips() {
        binding.chipTodos.setOnClickListener(v -> {
            filtroActivo = "total";
            aplicarFiltro("total", null);
        });

        binding.chipRecientes.setOnClickListener(v -> {
            filtroActivo = "recientes";
            aplicarFiltro("recientes", null);
        });

        binding.chipBajoStock.setOnClickListener(v -> {
            filtroActivo = "bajo_stock";
            aplicarFiltro("bajo_stock", null);
        });

        binding.chipPrecio.setOnClickListener(v -> {
            Chip chip = (Chip) v;
            if (chip.getText().toString().contains("↑")) {
                chip.setText("Precio ↓");
                consultasFragment.ordenarPorPrecioDescendente();
            } else {
                chip.setText("Precio ↑");
                consultasFragment.ordenarPorPrecioAscendente();
            }
        });
    }

    private void aplicarFiltro(String modo, String categoria) {
        // Asegurar que estamos en la pestaña Consultas
        binding.viewPager.setCurrentItem(0, false);

        Callback<List<Producto>> callback = new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body();
                    if (consultasFragment != null && consultasFragment.isAdded() && consultasFragment.getView() != null) {
                        consultasFragment.actualizarProductos(productos);
                    } else if (consultasFragment != null) {
                        consultasFragment.guardarProductosPendientes(productos);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        switch (modo) {
            case "total":
                productoService.obtenerTodosLosProductos().enqueue(callback);
                break;
            case "bajo_stock":
                productoService.obtenerProductosConMenorStock().enqueue(callback);
                break;
            case "recientes":
                productoService.productosRecientes().enqueue(callback);
                break;
            case "por_categoria":
                if (categoria != null) {
                    productoService.buscarPorNombreCategoria(categoria).enqueue(callback);
                } else {
                    Toast.makeText(this, "Categoría no especificada", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void buscarProductos() {
        String query = binding.etBuscar.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }
        filtroActivo = "consultas";
        productoService.buscarPorNombre(query).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    consultasFragment.actualizarProductos(response.body());
                    binding.viewPager.setCurrentItem(0);
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generarPdfSegunFiltro() {
        switch (filtroActivo) {
            case "total":
                descargarPdf(reporteService.descargarReporteTodos());
                break;
            case "recientes":
                descargarPdf(reporteService.descargarReporteProductosRecientes());
                break;
            case "bajo_stock":
                descargarPdf(reporteService.descargarReporteBajoStock());
                break;
            case "por_categoria":
                if (categoriaDesdeIntent != null) {
                    descargarPdf(reporteService.descargarReportePorCategoria(categoriaDesdeIntent));
                } else {
                    Toast.makeText(this, "Categoría no especificada", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(this, "Exportar PDF no disponible para este filtro", Toast.LENGTH_SHORT).show();
        }
    }

    private void descargarPdf(Call<ResponseBody> call) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String fileName = "reporte.pdf";
                        String contentDisposition = response.headers().get("Content-Disposition");
                        if (contentDisposition != null && contentDisposition.contains("filename=")) {
                            fileName = contentDisposition.split("filename=")[1].replace("\"", "").trim();
                        }

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                        if (uri == null) {
                            Toast.makeText(ProductosTabsActivity.this, "No se pudo guardar el archivo", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try (OutputStream os = getContentResolver().openOutputStream(uri);
                             InputStream is = response.body().byteStream()) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                            os.flush();
                        }

                        Toast.makeText(ProductosTabsActivity.this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(ProductosTabsActivity.this, "Error guardando PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductosTabsActivity.this, "Error al generar PDF", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProductosTabsActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // MÉTODO COMPLETAMENTE REESCRITO - Para corregir problemas de timing
    private void cargarCategorias(String modo, String categoriaReporte, boolean filtrarInmediatamente) {
        categoriaService.listarCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias = response.body();

                    // Crear fragmento de consultas
                    consultasFragment = CategoriaProductosFragment.newInstance(null);
                    pagerAdapter = new ProductoCategoriasPagerAdapter(ProductosTabsActivity.this, categorias, true);
                    pagerAdapter.setFragmentTodosPersonalizado(consultasFragment);
                    binding.viewPager.setAdapter(pagerAdapter);

                    binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            if (ignorarCambioPestaña) {
                                ignorarCambioPestaña = false;
                                return;
                            }
                            if (position == 0) {
                                filtroActivo = "consultas";
                            } else {
                                filtroActivo = "por_categoria";
                                categoriaDesdeIntent = categorias.get(position - 1).getNombre();
                            }
                        }
                    });

                    new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                            tab.setText(position == 0 ? "Consultas" : categorias.get(position - 1).getNombre())
                    ).attach();

                    // CORREGIDO - Aplicar filtro con delay para asegurar que el fragmento esté listo
                    if (desdeReporte || filtrarInmediatamente) {
                        // Ir siempre a la pestaña Consultas
                        binding.viewPager.setCurrentItem(0, false);

                        // Configurar filtro
                        if ("por_categoria".equals(modo)) {
                            filtroActivo = "por_categoria";
                            categoriaDesdeIntent = categoriaReporte;
                        } else {
                            filtroActivo = modo != null ? modo : "total";
                        }

                        // Aplicar filtro con un pequeño delay para asegurar que el fragmento esté completamente cargado
                        binding.viewPager.post(() -> {
                            // Esperar un momento más para que el fragmento termine de inicializarse
                            binding.viewPager.postDelayed(() -> {
                                aplicarFiltroEnConsultas(filtroActivo, categoriaReporte);
                            }, 300); // 300ms de delay
                        });

                        desdeReporte = false;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // MÉTODO MEJORADO - Mejor manejo del timing y estados del fragmento
    private void aplicarFiltroEnConsultas(String modo, String categoria) {
        android.util.Log.d("ProductosTabsActivity", "Aplicando filtro: " + modo + ", categoria: " + categoria);

        Callback<List<Producto>> callback = new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Producto> productos = response.body();
                    android.util.Log.d("ProductosTabsActivity", "Productos obtenidos: " + productos.size());

                    if (consultasFragment != null) {
                        // Intentar actualizar inmediatamente si el fragmento está listo
                        if (consultasFragment.isAdded() && consultasFragment.getView() != null) {
                            consultasFragment.actualizarProductos(productos);
                            android.util.Log.d("ProductosTabsActivity", "Fragmento actualizado inmediatamente");
                        } else {
                            // Si no está listo, guardar para actualizar después
                            consultasFragment.guardarProductosPendientes(productos);
                            android.util.Log.d("ProductosTabsActivity", "Productos guardados como pendientes");

                            // Intentar nuevamente después de un momento
                            binding.viewPager.postDelayed(() -> {
                                if (consultasFragment.isAdded() && consultasFragment.getView() != null) {
                                    consultasFragment.actualizarProductos(productos);
                                    android.util.Log.d("ProductosTabsActivity", "Fragmento actualizado en segundo intento");
                                }
                            }, 500);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(ProductosTabsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("ProductosTabsActivity", "Error al cargar productos: " + t.getMessage());
            }
        };

        // Ejecutar la consulta según el modo
        switch (modo) {
            case "total":
                android.util.Log.d("ProductosTabsActivity", "Cargando todos los productos");
                productoService.obtenerTodosLosProductos().enqueue(callback);
                break;
            case "bajo_stock":
                android.util.Log.d("ProductosTabsActivity", "Cargando productos con bajo stock");
                productoService.obtenerProductosConMenorStock().enqueue(callback);
                break;
            case "recientes":
                android.util.Log.d("ProductosTabsActivity", "Cargando productos recientes");
                productoService.productosRecientes().enqueue(callback);
                break;
            case "por_categoria":
                if (categoria != null) {
                    android.util.Log.d("ProductosTabsActivity", "Cargando productos de categoría: " + categoria);
                    productoService.buscarPorNombreCategoria(categoria).enqueue(callback);
                } else {
                    Toast.makeText(this, "Categoría no especificada", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                android.util.Log.w("ProductosTabsActivity", "Modo no reconocido: " + modo);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (itemId == R.id.navigation_inicio) {
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_reportes) {
                intent.putExtra("fragment", "reportes");
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.navigation_usuarios && SessionManager.getInstance().isAdmin()) {
                intent.putExtra("fragment", "usuarios");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de navegación: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}