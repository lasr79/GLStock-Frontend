package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.glstock.adapter.MovimientoAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.MovimientoService;
import com.example.glstock.api.ReporteService;
import com.example.glstock.databinding.ActivityReporteMovimientosBinding;
import com.example.glstock.model.Movimiento;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.glstock.R;
import com.example.glstock.ui.MainActivity;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Clase que muestra un reporte de movimientos
public class ReporteMovimientoActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private ActivityReporteMovimientosBinding binding;
    private MovimientoService movimientoService;
    private ReporteService reporteService;
    private MovimientoAdapter adapter;
    private String modoActivo = "";
    private String fechaDesde = "", fechaHasta = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReporteMovimientosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configuracion de toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Movimientos");
        }
        // Configuracion del RecyclerView
        binding.rvMovimientos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovimientoAdapter();
        binding.rvMovimientos.setAdapter(adapter);
        // Inicializar servicios
        movimientoService = ApiClient.getClient().create(MovimientoService.class);
        reporteService = ApiClient.getClient().create(ReporteService.class);
        // Configuracion de navegacion inferior
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
            MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
            if (usuariosItem != null) {
                usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
            }
        }
        // Boton de generacion de PDF
        binding.fabGenerarPdf.setOnClickListener(v -> generarPdf());
        // Obtener modo de funcionamiento
        String modo = getIntent().getStringExtra("modo");
        modoActivo = modo;
        if ("ultimos".equals(modo)) {
            mostrarUltimosMovimientos();
        } else {
            // Si es por fechas, obtener las fechas y cargar movimientos
            fechaDesde = getIntent().getStringExtra("desde");
            fechaHasta = getIntent().getStringExtra("hasta");
            if (fechaDesde != null && fechaHasta != null) {
                cargarMovimientos(fechaDesde, fechaHasta);
            } else {
                Toast.makeText(this, "Fechas inválidas", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    // Generar PDF dependiendo del modo activo
    private void generarPdf() {
        if ("ultimos".equals(modoActivo)) {
            descargarPdf(reporteService.descargarReporteMovimientosRecientes());
        } else if (!fechaDesde.isEmpty() && !fechaHasta.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            String desdeISO = fechaDesde + "T00:00:00";
            String hastaISO = fechaHasta + "T23:59:59";
            descargarPdf(reporteService.descargarReporteMovimientosPorFechas(desdeISO, hastaISO));
        } else {
            Toast.makeText(this, "No se puede generar el PDF", Toast.LENGTH_SHORT).show();
        }
    }
    // Descargar y guardar el PDF en almacenamiento local
    private void descargarPdf(Call<ResponseBody> call) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        InputStream is = response.body().byteStream();
                        File file = new File(getExternalFilesDir(null), "reporte_movimientos.pdf");
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        is.close();
                        Toast.makeText(ReporteMovimientoActivity.this, "PDF descargado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(ReporteMovimientoActivity.this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReporteMovimientoActivity.this, "Error al generar PDF", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ReporteMovimientoActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Cargar movimientos dentro de un rango de fechas
    private void cargarMovimientos(String desdeStr, String hastaStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime inicio = LocalDateTime.parse(desdeStr + "T00:00:00", formatter);
        LocalDateTime fin = LocalDateTime.parse(hastaStr + "T23:59:59", formatter);

        movimientoService.movimientosPorRango(inicio, fin).enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMovimientos(response.body());
                } else {
                    Toast.makeText(ReporteMovimientoActivity.this, "Sin resultados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(ReporteMovimientoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Mostrar los ultimos 10 movimientos registrados
    private void mostrarUltimosMovimientos() {
        movimientoService.ultimos10Movimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMovimientos(response.body());
                } else {
                    Toast.makeText(ReporteMovimientoActivity.this, "Sin resultados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(ReporteMovimientoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Volver a la pantalla anterior al hacer back
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    // Maneja de la navegacion inferior
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_inicio) {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        } else if (itemId == R.id.navigation_reportes) {
            startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "reportes").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        } else if (itemId == R.id.navigation_usuarios) {
            if (SessionManager.getInstance().isAdmin()) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "usuarios").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            }
        }
        return false;
    }
}

