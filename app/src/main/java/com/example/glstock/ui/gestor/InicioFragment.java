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
import com.example.glstock.api.UsuarioService;
import com.example.glstock.databinding.FragmentInicioBinding;
import com.example.glstock.model.Movimiento;
import com.example.glstock.model.Producto;
import com.example.glstock.model.Usuario;
import com.example.glstock.ui.LoginActivity;
import com.example.glstock.ui.admin.UsuarioDetalleActivity;
import com.example.glstock.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragmento principal que muestra el dashboard
public class InicioFragment extends Fragment {
    // ViewBinding para acceder a las vistas del layout
    private FragmentInicioBinding binding;
    // Servicios de API
    private ProductoService productoService;
    private MovimientoService movimientoService;
    private UsuarioService usuarioService;
    // Usuario que ha iniciado sesion
    private Usuario usuarioActual;

    // Infla el layout del fragmento
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Devuelve la vista raíz
    }

    // Metodo que se ejecuta cuando la vista esta lista
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa los servicios Retrofit
        productoService = ApiClient.getClient().create(ProductoService.class);
        movimientoService = ApiClient.getClient().create(MovimientoService.class);
        usuarioService = ApiClient.getClient().create(UsuarioService.class);

        // Configura los botones para navegar a otras pantallas
        binding.btnConsultarProductos.setOnClickListener(v -> navigateToProductos());
        binding.btnRegistrarMovimiento.setOnClickListener(v -> navigateToMovimientos());

        // Configura las cards del dashboard para ver reportes - CORREGIDO
        binding.cardTotalProductos.setOnClickListener(v -> navegarAReporte("total"));
        binding.cardBajoStock.setOnClickListener(v -> navegarAReporte("bajo_stock"));
        binding.cardUltimosMovimientos.setOnClickListener(v -> navegarAReporte("movimientos_recientes"));

        // Boton de cerrar sesion
        binding.btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance().clearAuthData(); // Limpia sesión
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Cierra actividades previas
            startActivity(intent);
            getActivity().finish();
        });

        // Carga datos del usuario y datos del dashboard
        cargarInformacionUsuario();
        loadDashboardData();
    }

    // Carga los datos del usuario logueado desde la API
    private void cargarInformacionUsuario() {
        String email = SessionManager.getInstance().getUserEmail();
        usuarioService.buscarPorCorreo(email).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    usuarioActual = response.body().get(0);
                    configurarInformacionUsuario();
                } else {
                    configurarInformacionUsuarioBasica();
                }
            }
            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                configurarInformacionUsuarioBasica(); // En caso de error, fallback a datos básicos
            }
        });
    }

    // Muestra los datos completos del usuario si se cargaron
    private void configurarInformacionUsuario() {
        if (usuarioActual != null) {
            String nombreCompleto = usuarioActual.getNombre();
            if (usuarioActual.getApellido() != null && !usuarioActual.getApellido().isEmpty()) {
                nombreCompleto += " " + usuarioActual.getApellido();
            }
            // Muestra nombre, email y rol
            binding.tvUserGreeting.setText("Hola, " + nombreCompleto);
            binding.tvUserEmail.setText(usuarioActual.getCorreo());
            binding.tvUserRole.setText(usuarioActual.getRol().toString());

            // Si se toca la tarjeta del usuario, abre la pantalla de detalle
            binding.userInfoCard.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), UsuarioDetalleActivity.class);
                intent.putExtra("usuario_objeto", usuarioActual);
                // Si no es admin, se abre en modo solo lectura
                if (!SessionManager.getInstance().isAdmin()) {
                    intent.putExtra("solo_lectura", true);
                }
                startActivity(intent);
            });
        }
    }

    // Muestra una versión basica de la informacion del usuario usando datos de sesion
    private void configurarInformacionUsuarioBasica() {
        String email = SessionManager.getInstance().getUserEmail();
        String role = SessionManager.getInstance().getUserRole();
        // Intenta mostrar solo el nombre antes del "@"
        String nombreUsuario = email;
        if (email.contains("@")) {
            nombreUsuario = email.substring(0, email.indexOf('@'));
            nombreUsuario = nombreUsuario.substring(0, 1).toUpperCase() + nombreUsuario.substring(1);
        }
        // Muestra los datos basicos
        binding.tvUserGreeting.setText("Hola, " + nombreUsuario);
        binding.tvUserEmail.setText(email);
        binding.tvUserRole.setText(role);

        // Mensaje si se intenta acceder al detalle
        binding.userInfoCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "No se pudo cargar la información completa del usuario", Toast.LENGTH_SHORT).show();
        });
    }

    // MÉTODO CORREGIDO - Navega a diferentes pantallas de reportes dependiendo del tipo
    private void navegarAReporte(String tipoReporte) {
        Intent intent;

        switch (tipoReporte) {
            case "total":
                intent = new Intent(getActivity(), ProductosTabsActivity.class);
                intent.putExtra("modo_reporte", "total");
                intent.putExtra("filtrar_inmediatamente", true); // AGREGADO
                startActivity(intent);
                break;

            case "bajo_stock":
                intent = new Intent(getActivity(), ProductosTabsActivity.class);
                intent.putExtra("modo_reporte", "bajo_stock");
                intent.putExtra("filtrar_inmediatamente", true); // AGREGADO
                startActivity(intent);
                break;

            case "movimientos_recientes":
                intent = new Intent(getActivity(), ReporteMovimientoActivity.class);
                intent.putExtra("modo", "ultimos");
                startActivity(intent);
                break;
        }
    }

    // Carga los datos de resumen (total productos, bajo stock y movimientos recientes)
    private void loadDashboardData() {
        // Consulta de productos con bajo stock
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

        // Consulta de ultimos 10 movimientos
        movimientoService.ultimos10Movimientos().enqueue(new Callback<List<Movimiento>>() {
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

        // Consulta del total de productos
        productoService.obtenerTodosLosProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.tvTotalProductos.setText(String.valueOf(response.body().size()));
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al obtener productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navegacion a pantalla de productos
    private void navigateToProductos() {
        startActivity(new Intent(getActivity(), ProductosTabsActivity.class));
    }

    // Navegacion a pantalla de movimientos
    private void navigateToMovimientos() {
        startActivity(new Intent(getActivity(), MovimientosActivity.class));
    }

    // Limpia el binding cuando se destruye la vista del fragmento
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}