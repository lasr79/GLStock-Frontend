package com.example.glstock.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.glstock.R;
import com.example.glstock.model.Producto;
import com.example.glstock.ui.gestor.ProductoDetalleActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Adaptador personalizado para mostrar productos en forma de tarjetas (cards) usando RecyclerView
public class ProductoCardAdapter extends RecyclerView.Adapter<ProductoCardAdapter.ProductoViewHolder> {

    private List<Producto> productos; // Lista de productos a mostrar
    private OnProductoClickListener listener; // Interfaz para manejar clics en un producto
    private Context context; // Contexto necesario para inflar vistas y usar Glide
    // Interfaz para comunicar al exterior que se hizo clic en un producto
    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }
    // Constructor: recibe el listener de clics
    public ProductoCardAdapter(OnProductoClickListener listener) {
        this.productos = new ArrayList<>();
        this.listener = listener;
    }
    // Actualiza la lista de productos y notifica a la vista
    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }
    // Ordena los productos por precio ascendente
    public void ordenarPorPrecioAscendente() {
        Collections.sort(productos, Comparator.comparing(Producto::getPrecio));
        notifyDataSetChanged();
    }
    // Ordena los productos por precio descendente
    public void ordenarPorPrecioDescendente() {
        Collections.sort(productos, Comparator.comparing(Producto::getPrecio).reversed());
        notifyDataSetChanged();
    }
    // Crea y devuelve el ViewHolder con el layout de la tarjeta del producto
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_producto_card, parent, false);
        return new ProductoViewHolder(view);
    }
    // Asocia los datos del producto con la vista (bind)
    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto);
    }
    // Devuelve la cantidad total de productos en la lista
    @Override
    public int getItemCount() {
        return productos.size();
    }
    // Clase interna ViewHolder que gestiona cada tarjeta
    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProducto;
        private TextView tvNombreProducto, tvCategoria, tvStock, tvPrecio;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);

            // Maneja de clic para notificar el producto seleccionado
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductoClick(productos.get(position)); // Llama al listener con el producto clicado
                }
            });
        }
        // Rellena los campos del layout con los datos del producto
        public void bind(Producto producto) {
            tvNombreProducto.setText(producto.getNombre());
            tvCategoria.setText(producto.getCategoria() != null ?
                    producto.getCategoria().getNombre() : "Sin categoría");
            tvStock.setText("Stock: " + producto.getCantidad());
            tvPrecio.setText(String.format("%.2f€", producto.getPrecio()));
            // Carga la imagen desde la URL usando Glide y no hay lo que halla en la base de datos (podria ser null)
            if (producto.getUrlImagen() != null && !producto.getUrlImagen().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(producto.getUrlImagen())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivProducto);
            } else {
                ivProducto.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}