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
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.io.OutputStream;
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

// Clase que muestra un reporte de movimientos con descarga directa sin pedir permisos
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

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Movimientos");
        }

        binding.rvMovimientos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovimientoAdapter();
        binding.rvMovimientos.setAdapter(adapter);

        movimientoService = ApiClient.getClient().create(MovimientoService.class);
        reporteService = ApiClient.getClient().create(ReporteService.class);

        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
            MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
            if (usuariosItem != null) {
                usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
            }
        }

        binding.fabGenerarPdf.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¿Estás seguro de que quieres descargar el reporte en PDF?");

            builder.setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    generarPdf();
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // solo cierra el diálogo
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        String modo = getIntent().getStringExtra("modo");
        modoActivo = modo;
        if ("ultimos".equals(modo)) {
            mostrarUltimosMovimientos();
        } else {
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

    private void descargarPdf(Call<ResponseBody> call) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String contentDisposition = response.headers().get("Content-Disposition");
                        String fileName = "reporte_movimientos.pdf";
                        if (contentDisposition != null && contentDisposition.contains("filename=")) {
                            fileName = contentDisposition.split("filename=")[1].replace("\"", "").trim();
                        }

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        ContentResolver resolver = getContentResolver();
                        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

                        if (uri != null) {
                            InputStream is = response.body().byteStream();
                            OutputStream os = resolver.openOutputStream(uri);

                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }

                            os.flush();
                            os.close();
                            is.close();

                            Toast.makeText(ReporteMovimientoActivity.this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ReporteMovimientoActivity.this, "Error accediendo a almacenamiento", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        Toast.makeText(ReporteMovimientoActivity.this, "Error guardando PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // MÉTODO CORREGIDO - Agregada validación para resultados vacíos
    private void cargarMovimientos(String desdeStr, String hastaStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime inicio = LocalDateTime.parse(desdeStr + "T00:00:00", formatter);
        LocalDateTime fin = LocalDateTime.parse(hastaStr + "T23:59:59", formatter);

        movimientoService.movimientosPorRango(inicio, fin).enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movimiento> movimientos = response.body();

                    // AGREGADO - Validación para resultados vacíos
                    if (movimientos.isEmpty()) {
                        mostrarMensajeNoResultados("No se encontraron movimientos en el rango de fechas seleccionado.");
                    } else {
                        adapter.setMovimientos(movimientos);
                    }
                } else {
                    mostrarMensajeNoResultados("Error al obtener los movimientos. Intente nuevamente.");
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                mostrarMensajeNoResultados("Error de conexión: " + t.getMessage());
            }
        });
    }

    // MÉTODO CORREGIDO - Agregada validación para resultados vacíos
    private void mostrarUltimosMovimientos() {
        movimientoService.ultimos10Movimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movimiento> movimientos = response.body();

                    // AGREGADO - Validación para resultados vacíos
                    if (movimientos.isEmpty()) {
                        mostrarMensajeNoResultados("No hay movimientos registrados en el sistema.");
                    } else {
                        adapter.setMovimientos(movimientos);
                    }
                } else {
                    mostrarMensajeNoResultados("Error al obtener los movimientos. Intente nuevamente.");
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                mostrarMensajeNoResultados("Error de conexión: " + t.getMessage());
            }
        });
    }

    // MÉTODO AGREGADO - Muestra mensaje cuando no hay resultados
    private void mostrarMensajeNoResultados(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Sin resultados")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Regresa a la pantalla anterior
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

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