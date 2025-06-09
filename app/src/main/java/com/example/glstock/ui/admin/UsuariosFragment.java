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
    private FragmentUsuariosBinding binding;
    private UsuarioAdapter adapter;
    private UsuarioService usuarioService;
    private Call<List<Usuario>> llamada;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsuariosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usuarioService = ApiClient.getClient().create(UsuarioService.class);
        adapter = new UsuarioAdapter(this);

        binding.rvUsuarios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsuarios.setAdapter(adapter);

        binding.btnBuscarUsuario.setOnClickListener(v -> buscarUsuarios());
        binding.btnNuevoUsuario.setOnClickListener(v -> navegarANuevoUsuario());

        binding.etBuscarUsuario.setHint("Introduzca el usuario");

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        showProgress(true);
        buscarUsuarios();
    }

    private void buscarUsuarios() {
        final String query = binding.etBuscarUsuario.getText().toString().trim().toLowerCase();
        showProgress(true);

        llamada = usuarioService.buscarUsuariosPorNombre("");
        llamada.enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                showProgress(false);
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Usuario> filtrados = new ArrayList<>();
                    for (Usuario u : response.body()) {
                        if (TextUtils.isEmpty(query) || u.getCorreo().toLowerCase().contains(query)) {
                            filtrados.add(u);
                        }
                    }
                    adapter.setUsuarios(filtrados);
                    if (filtrados.isEmpty()) {
                        mostrarMensajeNoResultados("No se encontraron usuarios con ese correo");
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al buscar usuarios: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                showProgress(false);
                if (!isAdded() || binding == null) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navegarANuevoUsuario() {
        Intent intent = new Intent(requireActivity(), UsuarioDetalleActivity.class);
        startActivity(intent);
    }

    private void mostrarMensajeNoResultados(String mensaje) {
        if (isAdded()) {
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        if (binding != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onUsuarioClick(Usuario usuario) {
        Intent intent = new Intent(requireActivity(), UsuarioDetalleActivity.class);
        intent.putExtra("usuario_objeto", usuario);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (llamada != null) llamada.cancel();
        binding = null;
    }
}

