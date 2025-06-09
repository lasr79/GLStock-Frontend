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

public class CategoriaProductosFragment extends Fragment implements ProductoCardAdapter.OnProductoClickListener {
    private static final String ARG_CATEGORIA_ID = "categoria_id";
    private static final String ARG_CATEGORIA_NOMBRE = "categoria_nombre";
    private boolean debeActualizarAlCargar = false;
    private FragmentCategoriaProductosBinding binding;
    private ProductoCardAdapter adapter;
    private ProductoService productoService;
    private Categoria categoria;
    private List<Producto> productosFiltrados = null;
    private List<Producto> productosPendientes = null;

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

        adapter = new ProductoCardAdapter(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        binding.rvProductosCategoria.setLayoutManager(layoutManager);
        binding.rvProductosCategoria.setAdapter(adapter);

        if (debeActualizarAlCargar && productosPendientes != null) {
            actualizarProductos(productosPendientes);
            productosPendientes = null;
            debeActualizarAlCargar = false;
        } else {
            cargarProductos();
        }
    }

    public void cargarProductos() {
        if (productosFiltrados != null) {
            adapter.setProductos(productosFiltrados);
            mostrarMensaje(productosFiltrados.isEmpty());
            return;
        }

        if (categoria != null) {
            Map<String, Long> datos = new HashMap<>();
            datos.put("idCategoria", categoria.getId());

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

    public void actualizarProductos(List<Producto> productos) {
        if (binding == null || adapter == null) {
            productosPendientes = productos;
        } else {
            productosFiltrados = productos;
            adapter.setProductos(productos);
            mostrarMensaje(productos.isEmpty());
        }
    }
    public void guardarProductosPendientes(List<Producto> productos) {
        this.productosPendientes = productos;

    }
    private void mostrarMensaje(boolean vacio) {
        if (binding != null) {
            binding.tvNoProducts.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.rvProductosCategoria.setVisibility(vacio ? View.GONE : View.VISIBLE);
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

    @Override
    public void onProductoClick(Producto producto) {
        try {
            Intent intent = new Intent(getContext(), ProductoDetalleActivity.class);
            intent.putExtra("producto_objeto", producto);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir detalle: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
