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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.glstock.R;
import com.example.glstock.adapter.ProductoCardAdapter;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.ProductoService;
import com.example.glstock.databinding.FragmentCategoriaProductosBinding;
import com.example.glstock.model.Categoria;
import com.example.glstock.model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Fragmento que muestra productos asociados a una categoría específica
public class CategoriaProductosFragment extends Fragment implements ProductoCardAdapter.OnProductoClickListener {
    // Claves para recibir datos por argumentos
    private static final String ARG_CATEGORIA_ID = "categoria_id";
    private static final String ARG_CATEGORIA_NOMBRE = "categoria_nombre";
    // ViewBinding para acceder al layout del fragmento
    private FragmentCategoriaProductosBinding binding;
    // Adaptador del RecyclerView que muestra los productos en tarjetas
    private ProductoCardAdapter adapter;
    private ProductoService productoService;
    // Categoria actual seleccionada
    private Categoria categoria;
    private List<Producto> productosFiltrados = null;
    // Metodo estático para crear una nueva instancia del fragmento con categoria
    public static CategoriaProductosFragment newInstance(Categoria categoria) {
        CategoriaProductosFragment fragment = new CategoriaProductosFragment();
        if (categoria != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_CATEGORIA_ID, categoria.getId());
            args.putString(ARG_CATEGORIA_NOMBRE, categoria.getNombre());
            fragment.setArguments(args);
        }
        return fragment;
    }
    // Se ejecuta al crearse el fragmento
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recupera los argumentos pasados (id y nombre de categoria)
        if (getArguments() != null && getArguments().containsKey(ARG_CATEGORIA_ID)) {
            long categoriaId = getArguments().getLong(ARG_CATEGORIA_ID);
            String categoriaNombre = getArguments().getString(ARG_CATEGORIA_NOMBRE);
            // Inicializa el objeto categoria
            categoria = new Categoria();
            categoria.setId(categoriaId);
            categoria.setNombre(categoriaNombre);
        }
        // Inicializa la api
        productoService = ApiClient.getClient().create(ProductoService.class);
    }
    // Infla el layout del fragmento usando ViewBinding
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriaProductosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    // Se ejecuta cuando la vista ya esta creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializa el adaptador del RecyclerView
        adapter = new ProductoCardAdapter(this); // Se pasa el listener de clics
        // Configura un diseño de grilla con 2 columnas
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.rvProductosCategoria.setLayoutManager(layoutManager);
        binding.rvProductosCategoria.setAdapter(adapter);
        // Carga los productos correspondientes a la categoria
        cargarProductos();
    }
    // Metodo para cargar productos
    public void cargarProductos() {
        if (productosFiltrados != null) {
            adapter.setProductos(productosFiltrados);
            mostrarMensaje(productosFiltrados.isEmpty());
            return;
        }
        // Si hay categoria seleccionada, busca productos por categoria
        if (categoria != null) {
            Map<String, Long> datos = new HashMap<>();
            datos.put("idCategoria", categoria.getId());
            // Llamada al backend para productos de una categoria espeiífica
            productoService.buscarPorCategoria(datos).enqueue(new Callback<List<Producto>>() {
                @Override
                public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Producto> productos = response.body();
                        adapter.setProductos(productos);
                        mostrarMensaje(productos.isEmpty());
                    } else {
                        Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<Producto>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // Si no hay categoria, obtiene todos los productos
            productoService.obtenerTodosLosProductos().enqueue(new Callback<List<Producto>>() {
                @Override
                public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Producto> productos = response.body();
                        adapter.setProductos(productos);
                        mostrarMensaje(productos.isEmpty());
                    } else {
                        Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Producto>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Permite actualizar la lista de productos desde fuera
    public void actualizarProductos(List<Producto> productos) {
        this.productosFiltrados = productos;
        if (adapter != null) {
            adapter.setProductos(productos);
            mostrarMensaje(productos.isEmpty());
        }
    }
    // Muestra u oculta el mensaje de "no hay productos"
    private void mostrarMensaje(boolean vacio) {
        binding.tvNoProducts.setVisibility(vacio ? View.VISIBLE : View.GONE);
        binding.rvProductosCategoria.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }
    // Ordena los productos por precio ascendente
    public void ordenarPorPrecioAscendente() {
        if (adapter != null) {
            adapter.ordenarPorPrecioAscendente();
        }
    }
    // Ordena los productos por precio descendente
    public void ordenarPorPrecioDescendente() {
        if (adapter != null) {
            adapter.ordenarPorPrecioDescendente();
        }
    }
    // Se ejecuta al hacer clic sobre un producto
    @Override
    public void onProductoClick(Producto producto) {
        try {
            // Abre la pantalla de detalle del producto seleccionado
            Intent intent = new Intent(getContext(), ProductoDetalleActivity.class);
            intent.putExtra("producto_objeto", producto);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir detalle: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    // Libera el binding al destruir la vista
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}