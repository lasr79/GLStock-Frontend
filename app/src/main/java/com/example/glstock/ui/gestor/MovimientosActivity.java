package com.example.glstock.ui.gestor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.MovimientoService;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.ActivityMovimientosBinding;
import com.example.glstock.model.Movimiento;
import com.example.glstock.model.Producto;
import com.example.glstock.model.TipoMovimiento;
import com.example.glstock.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovimientosActivity extends AppCompatActivity {

    private ActivityMovimientosBinding binding;
    private ProductoService productoService;
    private MovimientoService movimientoService;
    private List<Producto> listaProductos;
    private Producto productoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovimientosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Verificar si existe una Toolbar en el layout
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Registrar Movimiento");
            }
        }

        // Inicializar servicios
        productoService = ApiClient.getClient().create(ProductoService.class);
        movimientoService = ApiClient.getClient().create(MovimientoService.class);

        // Cargar productos
        cargarProductos();

        // Configurar spinner de productos
        binding.spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaProductos != null && !listaProductos.isEmpty()) {
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

        // Configurar los RadioButtons para tipo de movimiento
        binding.rgTipoMovimiento.setOnCheckedChangeListener((group, checkedId) -> {
            boolean esSalida = (checkedId == R.id.rbSalida);
            binding.etCantidad.setHint(esSalida ? "Cantidad a retirar" : "Cantidad a ingresar");
            binding.etMotivo.setHint(esSalida ? "Motivo de salida" : "Motivo de entrada");

            // Actualizar campo de validación
            actualizarDetallesProducto();
        });

        // Configurar botón de registro
        binding.btnRegistrar.setOnClickListener(v -> registrarMovimiento());
    }

    private void cargarProductos() {
        showProgress(true);
        productoService.productosRecientes().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaProductos = response.body();

                    // Crear un array con los nombres de los productos para el spinner
                    List<String> nombresProductos = new ArrayList<>();
                    for (Producto producto : listaProductos) {
                        nombresProductos.add(producto.getNombre());
                    }

                    // Configurar el adaptador del spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            MovimientosActivity.this,
                            android.R.layout.simple_spinner_item,
                            nombresProductos);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerProducto.setAdapter(adapter);

                    // Seleccionar el primer producto automáticamente
                    if (!nombresProductos.isEmpty()) {
                        binding.spinnerProducto.setSelection(0);
                    }
                } else {
                    Toast.makeText(MovimientosActivity.this,
                            "Error al cargar productos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarDetallesProducto() {
        if (productoSeleccionado != null) {
            binding.tvStockActual.setText("Stock actual: " + productoSeleccionado.getCantidad());

            // Verificar si es salida para mostrar cantidad disponible
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

    private void registrarMovimiento() {
        // Validar campos
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

        // Validar que la cantidad sea mayor que 0
        if (cantidad <= 0) {
            binding.etCantidad.setError("La cantidad debe ser mayor que 0");
            return;
        }

        // Si es una salida, validar que no exceda el stock
        if (binding.rbSalida.isChecked() && cantidad > productoSeleccionado.getCantidad()) {
            binding.etCantidad.setError("La cantidad excede el stock disponible");
            return;
        }

        // Determinar tipo de movimiento
        TipoMovimiento tipoMovimiento = binding.rbEntrada.isChecked() ?
                TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        showProgress(true);

        // Crear objeto movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setProducto(productoSeleccionado);

        // Crear usuario temporal con solo el ID (el backend llenará el resto)
        Usuario usuario = new Usuario();
        usuario.setId(1L); // Esto debería ser el ID del usuario logueado
        movimiento.setUsuario(usuario);

        movimiento.setTipo(tipoMovimiento);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);

        // Registrar movimiento
        movimientoService.registrarMovimiento(movimiento).enqueue(new Callback<Movimiento>() {
            @Override
            public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MovimientosActivity.this,
                            "Movimiento registrado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Volver a la pantalla anterior
                } else {
                    Toast.makeText(MovimientosActivity.this,
                            "Error al registrar movimiento: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movimiento> call, Throwable t) {
                showProgress(false);
                Toast.makeText(MovimientosActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegistrar.setEnabled(!show);
        binding.spinnerProducto.setEnabled(!show);
        binding.etCantidad.setEnabled(!show);
        binding.etMotivo.setEnabled(!show);
        binding.rgTipoMovimiento.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}