package com.example.glstock.ui.gestor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.example.glstock.api.ApiClient;
import com.example.glstock.api.CategoriaService;
import com.example.glstock.databinding.FragmentReportesBinding;
import com.example.glstock.model.Categoria;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.glstock.R;
//Fragment de el modulo de reportes
public class ReportesFragment extends Fragment {
    private FragmentReportesBinding binding;
    private CategoriaService categoriaService;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el layout del fragment usando ViewBinding
        binding = FragmentReportesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa el servicio de categorias
        categoriaService = ApiClient.getClient().create(CategoriaService.class);
        // Obtiene las referencias a los botones dentro de las tarjetas
        TextView btnTotalProductos = binding.cardTotalWrapper.findViewById(R.id.btnTotalProductos);
        TextView btnCategoria = binding.cardCategoriaWrapper.findViewById(R.id.btnCategoria);
        TextView btnBajoStock = binding.cardBajoStockWrapper.findViewById(R.id.btnBajoStock);
        TextView btnUltimosMovimientos = binding.cardUltimosMovimientosWrapper.findViewById(R.id.btnUltimosMovimientos);
        TextView btnMovimientosRecientes = binding.cardRecientesWrapper.findViewById(R.id.btnMovimientosRecientes);
        // Asigna acciones a cada boton
        btnTotalProductos.setOnClickListener(v -> manejarReporte("total", null, null, null));
        btnCategoria.setOnClickListener(v -> mostrarSelectorCategoria());
        btnBajoStock.setOnClickListener(v -> manejarReporte("bajo_stock", null, null, null));
        btnUltimosMovimientos.setOnClickListener(v -> mostrarSelectorFechas());
        btnMovimientosRecientes.setOnClickListener(v -> mostrarUltimosMovimientos());
    }
    // Maneja la navegacion segun el tipo de reporte seleccionado
    private void manejarReporte(String modo, String categoria, String desde, String hasta) {
        switch (modo) {
            case "movimientos":
                Intent intentMov = new Intent(getContext(), ReporteMovimientoActivity.class);
                intentMov.putExtra("desde", desde);
                intentMov.putExtra("hasta", hasta);
                startActivity(intentMov);
                break;
            default:
                Intent intent = new Intent(getContext(), ProductosTabsActivity.class);
                intent.putExtra("modo_reporte", modo);
                if ("por_categoria".equals(modo)) {
                    intent.putExtra("categoria", categoria);
                }
                startActivity(intent);
                break;
        }
    }
    // Muestra un dialogo para seleccionar una categoria
    private void mostrarSelectorCategoria() {
        categoriaService.listarCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Categoria> categorias = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (Categoria c : categorias) {
                        nombres.add(c.getNombre());
                    }
                    String[] nombresArray = nombres.toArray(new String[0]);
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Selecciona una categoría")
                            .setItems(nombresArray, (dialog, which) -> {
                                manejarReporte("por_categoria", nombresArray[which], null, null);
                            })
                            .show();
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("No se pudieron cargar las categorías: " + t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
    // Inicia la actividad para mostrar los ultimos movimientos
    private void mostrarUltimosMovimientos() {
        Intent intent = new Intent(getContext(), ReporteMovimientoActivity.class);
        intent.putExtra("modo", "ultimos");
        startActivity(intent);
    }
    // Muestra un dialogo personalizado para seleccionar rango de fechas
    private void mostrarSelectorFechas() {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_seleccionar_fechas, null);
        final TextView tvDesde = dialogView.findViewById(R.id.tvDesde);
        final TextView tvHasta = dialogView.findViewById(R.id.tvHasta);
        final Button btnConsultar = dialogView.findViewById(R.id.btnConsultar);
        btnConsultar.setEnabled(false);
        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona el rango de fechas")
                .setView(dialogView)
                .setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();
        // Configura el selector de fechas
        tvDesde.setOnClickListener(v -> mostrarDatePicker(tvDesde, dialog, tvHasta, btnConsultar));
        tvHasta.setOnClickListener(v -> mostrarDatePicker(tvHasta, dialog, tvDesde, btnConsultar));
        // Acción al pulsar consultar
        btnConsultar.setOnClickListener(v -> {
            String desde = tvDesde.getText().toString();
            String hasta = tvHasta.getText().toString();
            dialog.dismiss();
            manejarReporte("movimientos", null, desde, hasta);
        });
        dialog.show();
    }
    // Muestra el DatePicker y actualiza el campo de texto
    private void mostrarDatePicker(TextView target, AlertDialog dialog, TextView other, Button btnConsultar) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String fecha = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
            target.setText(fecha);

            if (!other.getText().toString().isEmpty()) {
                btnConsultar.setEnabled(true);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Libera el binding para evitar memory leaks
    }
}


