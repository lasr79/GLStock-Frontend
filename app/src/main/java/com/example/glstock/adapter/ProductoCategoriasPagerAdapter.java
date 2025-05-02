package com.example.glstock.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.glstock.model.Categoria;
import com.example.glstock.ui.gestor.CategoriaProductosFragment;

import java.util.ArrayList;
import java.util.List;

public class ProductoCategoriasPagerAdapter extends FragmentStateAdapter {

    private List<Categoria> categorias;
    private boolean incluyeTodas;

    public ProductoCategoriasPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Categoria> categorias, boolean incluyeTodas) {
        super(fragmentActivity);
        this.categorias = categorias != null ? categorias : new ArrayList<>();
        this.incluyeTodas = incluyeTodas;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (incluyeTodas && position == 0) {
            // Pestaña "Todos" muestra todos los productos
            return CategoriaProductosFragment.newInstance(null);
        } else {
            // Ajustar el índice si tenemos la pestaña "Todos"
            int categoriaIndex = incluyeTodas ? position - 1 : position;
            return CategoriaProductosFragment.newInstance(categorias.get(categoriaIndex));
        }
    }

    @Override
    public int getItemCount() {
        return incluyeTodas ? categorias.size() + 1 : categorias.size();
    }

    public Categoria getCategoria(int position) {
        if (incluyeTodas && position == 0) {
            return null; // "Todos" no tiene categoría
        } else {
            int categoriaIndex = incluyeTodas ? position - 1 : position;
            return categorias.get(categoriaIndex);
        }
    }
}