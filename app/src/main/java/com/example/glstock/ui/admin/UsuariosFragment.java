package com.example.glstock.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.glstock.adapter.UsuarioAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.UsuarioService;
import com.example.glstock.databinding.FragmentUsuariosBinding;
import com.example.glstock.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragmento que muestra y gestiona una lista de usuarios
public class UsuariosFragment extends Fragment implements UsuarioAdapter.OnUsuarioClickListener {
    // Binding para acceder a las vistas del layout sin usar findViewById
    private FragmentUsuariosBinding binding;
    // Adaptador para el RecyclerView
    private UsuarioAdapter adapter;
    // Servicio Retrofit para llamadas a la API
    private UsuarioService usuarioService;
    // Infla el layout del fragmento
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el binding con el layout del fragmento
        binding = FragmentUsuariosBinding.inflate(inflater, container, false);
        // Devuelve la vista raiz del layout
        return binding.getRoot();
    }
    // Se ejecuta cuando la vista ya ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa el servicio que se comunica con el backend
        usuarioService = ApiClient.getClient().create(UsuarioService.class);
        // Crea el adaptador y le pasa el listener (este fragmento)
        adapter = new UsuarioAdapter(this);
        // Configura el RecyclerView con un LinearLayout vertical
        binding.rvUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
        // Asigna el adaptador al RecyclerView
        binding.rvUsuarios.setAdapter(adapter);
        // Accion para el boton de buscar usuario
        binding.btnBuscarUsuario.setOnClickListener(v -> buscarUsuarios());
        // Accion para el boton de crear nuevo usuario
        binding.btnNuevoUsuario.setOnClickListener(v -> navegarANuevoUsuario());
        // Cambia el texto de ayuda en el campo de busqueda
        binding.etBuscarUsuario.setHint("Introduzca el usuario)");
        // Carga inicial de usuarios al abrir el fragmento
        cargarUsuarios();
    }
    // Muestra el progress y ejecuta busqueda de usuarios
    private void cargarUsuarios() {
        showProgress(true); // Muestra el indicador de carga
        buscarUsuarios();   // Llama al método de búsqueda
    }
    // Realiza la busqueda de usuarios
    private void buscarUsuarios() {
        // Obtiene el texto ingresado y lo convierte a minúsculas
        final String query = binding.etBuscarUsuario.getText().toString().trim().toLowerCase();
        // Muestra el ProgressBar
        showProgress(true);
        // Llama a la API para obtener todos los usuarios (sin filtro real en el backend)
        usuarioService.buscarUsuariosPorNombre("").enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Crea una lista para los usuarios filtrados
                    List<Usuario> filtrados = new ArrayList<>();
                    // Recorre todos los usuarios y filtra por correo
                    for (Usuario u : response.body()) {
                        if (TextUtils.isEmpty(query) || u.getCorreo().toLowerCase().contains(query)) {
                            filtrados.add(u);
                        }
                    }
                    // Muestra los usuarios en el RecyclerView
                    adapter.setUsuarios(filtrados);
                    // Si no hay resultados, muestra mensaje
                    if (filtrados.isEmpty()) {
                        mostrarMensajeNoResultados("No se encontraron usuarios con ese correo");
                    }
                } else {
                    // Muestra error si la respuesta no fue exitosa
                    Toast.makeText(getContext(), "Error al buscar usuarios: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                // Oculta el ProgressBar y muestra error de conexion
                showProgress(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Abre la actividad para crear un nuevo usuario
    private void navegarANuevoUsuario() {
        Intent intent = new Intent(getActivity(), UsuarioDetalleActivity.class);
        startActivity(intent);
    }
    // Muestra un Toast si no se encontraron resultados
    private void mostrarMensajeNoResultados(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
    // Muestra u oculta el ProgressBar
    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    // Se llama cuando se hace clic en un usuario del RecyclerView
    @Override
    public void onUsuarioClick(Usuario usuario) {
        // Abre la actividad de detalle y pasa el usuario seleccionado
        Intent intent = new Intent(getActivity(), UsuarioDetalleActivity.class);
        intent.putExtra("usuario_objeto", usuario);
        startActivity(intent);
    }
    // Limpia el binding cuando se destruye la vista
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
