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

public class UsuariosFragment extends Fragment implements UsuarioAdapter.OnUsuarioClickListener {

    private FragmentUsuariosBinding binding;
    private UsuarioAdapter adapter;
    private UsuarioService usuarioService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsuariosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar servicios
        usuarioService = ApiClient.getClient().create(UsuarioService.class);

        // Configurar RecyclerView
        adapter = new UsuarioAdapter(this);
        binding.rvUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsuarios.setAdapter(adapter);

        // Configurar botones
        binding.btnBuscarUsuario.setOnClickListener(v -> buscarUsuarios());
        binding.btnNuevoUsuario.setOnClickListener(v -> navegarANuevoUsuario());

        // Configurar radiobuttons
        binding.rgFiltroUsuario.setOnCheckedChangeListener((group, checkedId) -> {
            String hint = "Buscar......";
            if (checkedId == binding.rbId.getId()) {
                hint = "Buscar por ID...";
            } else if (checkedId == binding.rbNombre.getId()) {
                hint = "Buscar por nombre...";
            } else if (checkedId == binding.rbEmail.getId()) {
                hint = "Buscar por email...";
            }
            binding.etBuscarUsuario.setHint(hint);
        });

        // Cargar usuarios
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        showProgress(true);
        // Para cargar todos los usuarios necesitaríamos un endpoint que los liste todos
        // Por ahora, usaremos la búsqueda con un término vacío para obtener todos
        buscarUsuarios();
    }

    private void buscarUsuarios() {
        String query = binding.etBuscarUsuario.getText().toString().trim();

        if (TextUtils.isEmpty(query)) {
            query = ""; // Búsqueda vacía para obtener todos
        }

        showProgress(true);

        // Dependiendo del radiobutton seleccionado, realizamos diferentes tipos de búsqueda
        // Nota: En tu backend solo tienes búsqueda por nombre, así que para ID y Email
        // necesitarías implementar esos endpoints

        if (binding.rbId.isChecked() && !TextUtils.isEmpty(query)) {
            // Buscar por ID (simular esto filtrando resultados)
            try {
                Long id = Long.parseLong(query);
                buscarUsuariosPorNombre("").enqueue(new Callback<List<Usuario>>() {
                    @Override
                    public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                        showProgress(false);
                        if (response.isSuccessful() && response.body() != null) {
                            // Filtrar manualmente para encontrar el ID
                            Usuario usuarioEncontrado = null;
                            for (Usuario usuario : response.body()) {
                                if (usuario.getId().equals(id)) {
                                    usuarioEncontrado = usuario;
                                    break;
                                }
                            }

                            List<Usuario> usuarios = new ArrayList<>();
                            if (usuarioEncontrado != null) {
                                usuarios.add(usuarioEncontrado);
                            }

                            adapter.setUsuarios(usuarios);
                            if (usuarios.isEmpty()) {
                                mostrarMensajeNoResultados("No se encontró usuario con ese ID");
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al buscar usuarios: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Usuario>> call, Throwable t) {
                        showProgress(false);
                        Toast.makeText(getContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                showProgress(false);
                Toast.makeText(getContext(), "ID inválido", Toast.LENGTH_SHORT).show();
            }
        } else if (binding.rbEmail.isChecked()) {
            // Buscar por Email (simular esto filtrando resultados)
            String finalQuery = query;
            buscarUsuariosPorNombre("").enqueue(new Callback<List<Usuario>>() {
                @Override
                public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                    showProgress(false);
                    if (response.isSuccessful() && response.body() != null) {
                        // Filtrar manualmente para encontrar emails que coincidan
                        List<Usuario> usuariosFiltrados = new ArrayList<>();
                        for (Usuario usuario : response.body()) {
                            if (TextUtils.isEmpty(finalQuery) ||
                                    usuario.getCorreo().toLowerCase().contains(finalQuery.toLowerCase())) {
                                usuariosFiltrados.add(usuario);
                            }
                        }

                        adapter.setUsuarios(usuariosFiltrados);
                        if (usuariosFiltrados.isEmpty()) {
                            mostrarMensajeNoResultados("No se encontraron usuarios con ese email");
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error al buscar usuarios: " + response.message(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Usuario>> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(getContext(),
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Buscar por nombre (método por defecto)
            buscarUsuariosPorNombre(query).enqueue(new Callback<List<Usuario>>() {
                @Override
                public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                    showProgress(false);
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.setUsuarios(response.body());
                        if (response.body().isEmpty()) {
                            mostrarMensajeNoResultados("No se encontraron usuarios con ese nombre");
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error al buscar usuarios: " + response.message(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Usuario>> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(getContext(),
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Call<List<Usuario>> buscarUsuariosPorNombre(String nombre) {
        return usuarioService.buscarUsuariosPorNombre(nombre);
    }

    private void navegarANuevoUsuario() {
        // Intent para navegar a la actividad de creación de usuarios
        // Intent intent = new Intent(getActivity(), NuevoUsuarioActivity.class);
        // startActivity(intent);

        // Por ahora, solo mostramos un mensaje
        Toast.makeText(getContext(),
                "Funcionalidad de crear usuario no implementada",
                Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensajeNoResultados(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUsuarioClick(Usuario usuario) {
        // Aquí podrías mostrar opciones para editar o eliminar el usuario
        Toast.makeText(getContext(),
                "Usuario seleccionado: " + usuario.getNombre(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}