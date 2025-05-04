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

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar navegación inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Mostrar u ocultar la sección de usuarios según el rol
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }

        // Cargar fragmento inicial o el fragmento especificado por el intent
        if (savedInstanceState == null) {
            if (getIntent().hasExtra("fragment")) {
                String fragmentToLoad = getIntent().getStringExtra("fragment");
                if ("reportes".equals(fragmentToLoad)) {
                    loadFragment(new ReportesFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.navigation_reportes);
                } else if ("usuarios".equals(fragmentToLoad) && SessionManager.getInstance().isAdmin()) {
                    loadFragment(new UsuariosFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.navigation_usuarios);
                } else {
                    loadFragment(new InicioFragment());
                }
            } else {
                loadFragment(new InicioFragment());
            }
        }
    }

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

        return loadFragment(fragment);
    }

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