package com.example.glstock.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.glstock.R;
import com.example.glstock.databinding.ActivityMainBinding;
import com.example.glstock.ui.admin.UsuariosFragment;
import com.example.glstock.ui.gestor.InicioFragment;
import com.example.glstock.ui.gestor.ReportesFragment;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    // ViewBinding para acceder a las vistas de activity_main.xml
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar el layout usando ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configurar la navegacion inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        // Mostrar u ocultar el item de usuarios dependiendo del rol del usuario (solo visible si es admin)
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }
        if (savedInstanceState == null) {
            // Comprobar si el intent trae un fragmento especifico para mostrar
            if (getIntent().hasExtra("fragment")) {
                String fragmentToLoad = getIntent().getStringExtra("fragment");
                if ("reportes".equals(fragmentToLoad)) {
                    // Cargar fragmento de reportes y seleccionar su ítem en la barra
                    loadFragment(new ReportesFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.navigation_reportes);
                } else if ("usuarios".equals(fragmentToLoad) && SessionManager.getInstance().isAdmin()) {
                    // Cargar fragmento de usuarios si el usuario es admin
                    loadFragment(new UsuariosFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.navigation_usuarios);
                } else {
                    // Si no hay coincidencia, cargar el fragmento de inicio
                    loadFragment(new InicioFragment());
                }
            } else {
                // Si no se paso un fragmento por el intent carga el de inicio por defecto
                loadFragment(new InicioFragment());
            }
        }
    }
    // Manejar la seleccion de items en la barra de navegacion inferior
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_inicio) {
            fragment = new InicioFragment();
        } else if (itemId == R.id.navigation_reportes) {
            fragment = new ReportesFragment();
        } else if (itemId == R.id.navigation_usuarios) {
            fragment = new UsuariosFragment();
        }
        // Llamar al metodo que reemplaza el fragmento actual con el seleccionado
        return loadFragment(fragment);
    }
    // Metodo auxiliar para reemplazar el fragmento actual en el contenedor con el nuevo
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
