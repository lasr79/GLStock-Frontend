package com.example.glstock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.glstock.R;
import com.example.glstock.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<Usuario> usuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }

    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.usuarios = new ArrayList<>();
        this.listener = listener;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);
        holder.bind(usuario);
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombreUsuario;
        private TextView tvId;
        private TextView tvEmail;
        private TextView tvRol;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreUsuario = itemView.findViewById(R.id.tvNombreUsuario);
            tvId = itemView.findViewById(R.id.tvId);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRol = itemView.findViewById(R.id.tvRol);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUsuarioClick(usuarios.get(position));
                }
            });
        }

        public void bind(Usuario usuario) {
            String nombreCompleto = usuario.getNombre();
            if (usuario.getApellido() != null && !usuario.getApellido().isEmpty()) {
                nombreCompleto += " " + usuario.getApellido();
            }

            tvNombreUsuario.setText(nombreCompleto);
            tvId.setText(String.valueOf(usuario.getId()));
            tvEmail.setText(usuario.getCorreo());
            tvRol.setText(usuario.getRol().toString());
        }
    }
}