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

// Adaptador para mostrar una lista de objetos Usuario en un RecyclerView
public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {
    private List<Usuario> usuarios;
    // Listener para manejar clics en los ítems
    private OnUsuarioClickListener listener;
    // Interfaz para manejar el clic sobre un usuario
    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }
    // Constructor del adaptador, recibe el listener como parametro
    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.usuarios = new ArrayList<>(); // Inicializa la lista como vacía
        this.listener = listener; // Asigna el listener recibido
    }
    // Metodo para actualizar la lista de usuarios
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios; // Asigna la nueva lista
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
    }
    // Crea un nuevo ViewHolder cuando se necesita una nueva fila
    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout de cada ítem de la lista
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view); // Devuelve el ViewHolder creado
    }
    // Asocia los datos del usuario con el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position); // Obtiene el usuario actual
        holder.bind(usuario); // Llama al método que carga los datos en la vista
    }
    // Devuelve la cantidad total de usuarios en la lista
    @Override
    public int getItemCount() {
        return usuarios.size();
    }
    // Clase interna que representa la vista de cada ítem
    class UsuarioViewHolder extends RecyclerView.ViewHolder {
        // Referencias a los elementos de la vista
        private TextView tvNombreUsuario;
        private TextView tvId;
        private TextView tvEmail;
        private TextView tvRol;
        // Constructor del ViewHolder
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);

            // Asigna las vistas a las variables
            tvNombreUsuario = itemView.findViewById(R.id.tvNombreUsuario);
            tvId = itemView.findViewById(R.id.tvId);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRol = itemView.findViewById(R.id.tvRol);
            // Maneja el clic sobre el ítem completo
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUsuarioClick(usuarios.get(position));
                }
            });
        }
        // Metodo para cargar los datos del usuario en las vistas
        public void bind(Usuario usuario) {
            // Construye el nombre completo del usuario
            String nombreCompleto = usuario.getNombre();
            if (usuario.getApellido() != null && !usuario.getApellido().isEmpty()) {
                nombreCompleto += " " + usuario.getApellido();
            }
            // Muestra los datos en los TextView
            tvNombreUsuario.setText(nombreCompleto);
            tvId.setText(String.valueOf(usuario.getId()));
            tvEmail.setText(usuario.getCorreo());
            tvRol.setText(usuario.getRol().toString());
        }
    }
}
