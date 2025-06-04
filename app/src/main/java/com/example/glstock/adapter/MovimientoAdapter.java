package com.example.glstock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.glstock.model.Movimiento;

import java.util.ArrayList;
import java.util.List;
import com.example.glstock.R;
// Adaptador para mostrar una lista de movimientos en un RecyclerView
public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {
    // Lista de movimientos a mostrar
    private List<Movimiento> movimientos = new ArrayList<>();
    // Metodo para actualizar la lista de movimientos y refrescar la vista
    public void setMovimientos(List<Movimiento> lista) {
        this.movimientos = lista;
        notifyDataSetChanged();
    }
    // Crea una nueva instancia del ViewHolder inflando el layout de cada item
    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño del item individual (item_movimiento.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(view); // Devuelve el ViewHolder
    }
    // Asocia los datos de un movimiento a los elementos visuales del ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        // Obtiene el movimiento en la posicion actual
        Movimiento mov = movimientos.get(position);
        // Asigna valores a los TextViews del layout
        holder.tvFecha.setText("Fecha: " + mov.getFecha().toLocalDate().toString());
        holder.tvTipo.setText("Tipo: " + mov.getTipo().name());
        holder.tvProducto.setText("Producto: " + mov.getProducto().getNombre());
        holder.tvCategoria.setText("Categoría: " + mov.getProducto().getCategoria().getNombre());
        holder.tvUsuario.setText("Registrado por: " + mov.getUsuario().getCorreo());
    }
    // Devuelve la cantidad total de ítems en la lista
    @Override
    public int getItemCount() {
        return movimientos.size();
    }
    // ViewHolder que representa un item individual de movimiento
    public static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        // Vistas del layout
        TextView tvFecha, tvTipo, tvProducto, tvCategoria, tvUsuario;
        // Constructor que vincula las vistas del layout con las variables
        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvProducto = itemView.findViewById(R.id.tvProducto);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
        }
    }
}