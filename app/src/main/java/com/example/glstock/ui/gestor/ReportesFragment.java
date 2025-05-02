package com.example.glstock.ui.gestor;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.glstock.api.ApiClient;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.FragmentReportesBinding;
import com.example.glstock.model.Producto;
import com.example.glstock.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportesFragment extends Fragment {

    private FragmentReportesBinding binding;
    private ProductoService productoService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar servicios
        productoService = ApiClient.getClient().create(ProductoService.class);

        // Configurar botones
        binding.btnTotalProductos.setOnClickListener(v -> mostrarTotalProductos());
        binding.btnCategoria.setOnClickListener(v -> mostrarProductosPorCategoria());
        binding.btnBajoStock.setOnClickListener(v -> mostrarProductosBajoStock());
        binding.btnUltimosMovimientos.setOnClickListener(v -> mostrarUltimosMovimientos());
        binding.btnDescargarReporte.setOnClickListener(v -> descargarReportePDF());
    }

    private void mostrarTotalProductos() {
        // Usar límite alto para obtener todos
        Map<String, Object> params = new HashMap<>();
        params.put("limite", 1000);

        productoService.productosMenorStock(params).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int total = response.body().size();
                    Toast.makeText(getContext(),
                            "Total de productos: " + total, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Error al obtener productos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarProductosPorCategoria() {
        // Aquí podrías abrir un nuevo diálogo o actividad para seleccionar categoría
        Toast.makeText(getContext(),
                "Funcionalidad no implementada", Toast.LENGTH_SHORT).show();
    }

    private void mostrarProductosBajoStock() {
        productoService.obtenerProductosConMenorStock().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder mensaje = new StringBuilder("Productos con bajo stock:\n");
                    for (Producto producto : response.body()) {
                        mensaje.append(producto.getNombre())
                                .append(": ")
                                .append(producto.getCantidad())
                                .append("\n");
                    }
                    Toast.makeText(getContext(), mensaje.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),
                            "Error al obtener productos con bajo stock", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarUltimosMovimientos() {
        // Aquí podrías abrir una nueva actividad para mostrar los últimos movimientos
        Toast.makeText(getContext(),
                "Funcionalidad no implementada", Toast.LENGTH_SHORT).show();
    }

    private void descargarReportePDF() {
        // Esta parte requeriría una implementación más compleja para manejar la descarga del PDF
        // Podrías usar DownloadManager de Android para descargar el archivo PDF
        Toast.makeText(getContext(),
                "Iniciando descarga del reporte...", Toast.LENGTH_SHORT).show();

        // Ejemplo básico de cómo hacerlo:
        /*
        String url = "http://10.0.2.2:8080/api/reportes/productos";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Reporte de Productos");
        request.setDescription("Descargando reporte");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "productos.pdf");

        DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
       downloadManager.enqueue(request);
       */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}