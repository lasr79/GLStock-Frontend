package com.example.glstock.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.glstock.model.Categoria;
import com.example.glstock.ui.gestor.CategoriaProductosFragment;

import java.util.ArrayList;
import java.util.List;
//Adaptador personalizado para ViewPager2 que maneja pestañas de categorías de productos y la pestaña de consultas (es donde muestra las consultas que se hagan)
public class ProductoCategoriasPagerAdapter extends FragmentStateAdapter {
    // Lista de categorias que se usaran para crear pestañas dinamicamente
    private List<Categoria> categorias;
    //Lo uso para incluir pestaña consultas
    private boolean incluyeConsultas;
    private CategoriaProductosFragment consultasFragment;
    public ProductoCategoriasPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Categoria> categorias, boolean incluyeConsultas) {
        super(fragmentActivity);
        this.categorias = categorias != null ? categorias : new ArrayList<>();
        this.incluyeConsultas = incluyeConsultas;
    }
    //Fragmento personalizado para la pestaña de consultas
    public void setFragmentTodosPersonalizado(CategoriaProductosFragment fragment) {
        this.consultasFragment = fragment;
    }
    // Crea el fragmento correspondiente a la posicion del ViewPager2 y controla la posicon de la pestaña que este
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (incluyeConsultas && position == 0) {
            // Devuelve el fragmento personalizado si existe, si no crea uno nuevo sin categoria
            return consultasFragment != null ? consultasFragment : CategoriaProductosFragment.newInstance(null);
        } else {
            // Si no es la pestaña de consultas, se calcula el indice correcto de categoria (restando 1 si hay consulta)
            int categoriaIndex = incluyeConsultas ? position - 1 : position;
            // Se crea un nuevo fragmento con la categoría correspondiente
            return CategoriaProductosFragment.newInstance(categorias.get(categoriaIndex));
        }
    }
    // Devuelve el numero total de pestañas que tendra el ViewPager2
    @Override
    public int getItemCount() {
        //Si se incluye la pestaña Consultas, se suma 1 al numero de categorias
        return incluyeConsultas ? categorias.size() + 1 : categorias.size();
    }

    }
