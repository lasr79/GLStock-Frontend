// app/src/main/java/com/example/glstock/ui/BaseActivity.java
package com.example.glstock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.glstock.R;
import com.example.glstock.databinding.ActivityBaseBinding;
import com.example.glstock.ui.admin.UsuariosFragment;
import com.example.glstock.ui.gestor.InicioFragment;
import com.example.glstock.ui.gestor.ReportesFragment;
import com.example.glstock.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected ActivityBaseBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getTitleForActivity());

        // Configurar navegación inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Mostrar u ocultar la sección de usuarios según el rol
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }

        // Cargar contenido en el contenedor
        View contentView = getLayoutInflater().inflate(getContentLayoutId(), binding.contentContainer, false);
        binding.contentContainer.addView(contentView);

        // Inicializar vistas
        initViews(contentView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_inicio) {
            navigateToActivity(MainActivity.class);
            return true;
        } else if (itemId == R.id.navigation_reportes) {
            // Por ahora, vamos a usar MainActivity con el fragmento de reportes
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "reportes");
            startActivity(intent);
            return true;
        } else if (itemId == R.id.navigation_usuarios) {
            if (SessionManager.getInstance().isAdmin()) {
                // Por ahora, vamos a usar MainActivity con el fragmento de usuarios
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment", "usuarios");
                startActivity(intent);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método para navegar a otra actividad
    protected void navigateToActivity(Class<?> activityClass) {
        if (this.getClass().equals(activityClass)) {
            return; // Ya estamos en esta actividad
        }

        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    // Métodos abstractos que deben implementar las subclases
    protected abstract int getContentLayoutId();
    protected abstract String getTitleForActivity();
    protected abstract void initViews(View contentView);
}