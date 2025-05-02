package com.example.glstock.ui.gestor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.glstock.api.ApiClient;
import com.example.glstock.api.MovimientoService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.FragmentInicioBinding;
import com.example.glstock.model.Movimiento;
import com.example.glstock.model.Producto;
import com.example.glstock.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private ProductoService productoService;
    private MovimientoService movimientoService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        movimientoService = ApiClient.getClient().create(MovimientoService.class);

        // Configurar saludo al usuario
        String nombreUsuario = SessionManager.getInstance().getUserEmail();
        binding.tvUserGreeting.setText("Hola, " + nombreUsuario);

        // Configurar botones
        binding.btnConsultarProductos.setOnClickListener(v -> navigateToProductos());
        binding.btnRegistrarMovimiento.setOnClickListener(v -> navigateToMovimientos());

        // Cargar datos
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Obtener productos con bajo stock
        productoService.obtenerProductosConMenorStock().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvBajoStock.setText(String.valueOf(response.body().size()));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Obtener últimos movimientos
        movimientoService.movimientosUltimos10Dias().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvUltimosMovimientos.setText(String.valueOf(response.body().size()));
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Contar productos totales (usamos un límite alto para obtener todos)
        Map<String, Object> params = new HashMap<>();
        params.put("limite", 1000); // Un número alto para obtener todos
        productoService.productosMenorStock(params).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvTotalProductos.setText(String.valueOf(response.body().size()));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToProductos() {
        // Cambio: Usar la nueva actividad con pestañas en lugar de la antigua
        startActivity(new Intent(getActivity(), ProductosTabsActivity.class));
    }

    private void navigateToMovimientos() {
        startActivity(new Intent(getActivity(), MovimientosActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}