package com.example.glstock.ui.gestor;

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

public class CategoriaProductosFragment extends Fragment implements ProductoCardAdapter.OnProductoClickListener {

    private static final String ARG_CATEGORIA_ID = "categoria_id";
    private static final String ARG_CATEGORIA_NOMBRE = "categoria_nombre";

    private FragmentCategoriaProductosBinding binding;
    private ProductoCardAdapter adapter;
    private ProductoService productoService;
    private Categoria categoria;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_CATEGORIA_ID)) {
            long categoriaId = getArguments().getLong(ARG_CATEGORIA_ID);
            String categoriaNombre = getArguments().getString(ARG_CATEGORIA_NOMBRE);

            categoria = new Categoria();
            categoria.setId(categoriaId);
            categoria.setNombre(categoriaNombre);
        }

        productoService = ApiClient.getClient().create(ProductoService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriaProductosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView con GridLayoutManager
        adapter = new ProductoCardAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2); // 2 columnas
        binding.rvProductosCategoria.setLayoutManager(layoutManager);
        binding.rvProductosCategoria.setAdapter(adapter);

        // Cargar productos según la categoría
        cargarProductos();
    }

    private void cargarProductos() {
        if (categoria != null) {
            // Cargar productos por categoría
            Map<String, Long> datos = new HashMap<>();
            datos.put("idCategoria", categoria.getId());

            productoService.buscarPorCategoria(datos).enqueue(new Callback<List<Producto>>() {
                @Override
                public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Producto> productos = response.body();
                        adapter.setProductos(productos);

                        // Mostrar mensaje si no hay productos
                        if (productos.isEmpty()) {
                            binding.tvNoProducts.setVisibility(View.VISIBLE);
                            binding.rvProductosCategoria.setVisibility(View.GONE);
                        } else {
                            binding.tvNoProducts.setVisibility(View.GONE);
                            binding.rvProductosCategoria.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Producto>> call, Throwable t) {
                    Toast.makeText(getContext(),
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Cargar todos los productos (pestaña "Todos")
            productoService.productosRecientes().enqueue(new Callback<List<Producto>>() {
                @Override
                public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Producto> productos = response.body();
                        adapter.setProductos(productos);

                        // Mostrar mensaje si no hay productos
                        if (productos.isEmpty()) {
                            binding.tvNoProducts.setVisibility(View.VISIBLE);
                            binding.rvProductosCategoria.setVisibility(View.GONE);
                        } else {
                            binding.tvNoProducts.setVisibility(View.GONE);
                            binding.rvProductosCategoria.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Producto>> call, Throwable t) {
                    Toast.makeText(getContext(),
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onProductoClick(Producto producto) {
        Toast.makeText(getContext(), "Producto seleccionado: " + producto.getNombre(),
                Toast.LENGTH_SHORT).show();
        // Aquí podrías abrir una actividad de detalle del producto o mostrar un diálogo
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Añadir estos métodos dentro de la clase CategoriaProductosFragment

    public void actualizarProductos(List<Producto> productos) {
        if (adapter != null) {
            adapter.setProductos(productos);

            // Mostrar mensaje si no hay productos
            if (productos.isEmpty()) {
                binding.tvNoProducts.setVisibility(View.VISIBLE);
                binding.rvProductosCategoria.setVisibility(View.GONE);
            } else {
                binding.tvNoProducts.setVisibility(View.GONE);
                binding.rvProductosCategoria.setVisibility(View.VISIBLE);
            }
        }
    }

    public void ordenarPorPrecioAscendente() {
        if (adapter != null) {
            adapter.ordenarPorPrecioAscendente();
        }
    }

    public void ordenarPorPrecioDescendente() {
        if (adapter != null) {
            adapter.ordenarPorPrecioDescendente();
        }
    }
}