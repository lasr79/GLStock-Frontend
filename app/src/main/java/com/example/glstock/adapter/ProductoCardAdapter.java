package com.example.glstock.adapter;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductoCardAdapter extends RecyclerView.Adapter<ProductoCardAdapter.ProductoViewHolder> {

    private List<Producto> productos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    public ProductoCardAdapter(OnProductoClickListener listener) {
        this.productos = new ArrayList<>();
        this.listener = listener;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }

    public void ordenarPorPrecioAscendente() {
        Collections.sort(productos, Comparator.comparing(Producto::getPrecio));
        notifyDataSetChanged();
    }

    public void ordenarPorPrecioDescendente() {
        Collections.sort(productos, Comparator.comparing(Producto::getPrecio).reversed());
        notifyDataSetChanged();
    }

    public void ordenarPorStockAscendente() {
        Collections.sort(productos, Comparator.comparing(Producto::getCantidad));
        notifyDataSetChanged();
    }

    public void ordenarPorNombre() {
        Collections.sort(productos, Comparator.comparing(Producto::getNombre));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_card, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProducto;
        private TextView tvNombreProducto;
        private TextView tvCategoria;
        private TextView tvStock;
        private TextView tvPrecio;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductoClick(productos.get(position));
                }
            });
        }

        public void bind(Producto producto) {
            tvNombreProducto.setText(producto.getNombre());
            tvCategoria.setText(producto.getCategoria() != null ?
                    producto.getCategoria().getNombre() : "Sin categoría");
            tvStock.setText("Stock: " + producto.getCantidad());
            tvPrecio.setText(String.format("%.2f€", producto.getPrecio()));

            // Cargar imagen con Glide
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