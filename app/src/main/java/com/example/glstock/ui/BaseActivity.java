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
    // ViewBinding para acceder a los elementos del layout base
    protected ActivityBaseBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Infla el layout base y lo asigna como contenido de la actividad
        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configura el toolbar como ActionBar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        // Agrega boton de retroceso en el ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Establece el título de la actividad usando un metodo abstracto que cada subclase implementa
        getSupportActionBar().setTitle(getTitleForActivity());
        // Configura el listener de la barra de navegacion inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        // Oculta o muestra el ítem de usuarios segun el rol del usuario (solo visible para admin)
        MenuItem usuariosItem = binding.bottomNavigation.getMenu().findItem(R.id.navigation_usuarios);
        if (usuariosItem != null) {
            usuariosItem.setVisible(SessionManager.getInstance().isAdmin());
        }
        // Infla el contenido especifico de la subclase dentro del contenedor del layout base
        View contentView = getLayoutInflater().inflate(getContentLayoutId(), binding.contentContainer, false);
        binding.contentContainer.addView(contentView);
        // Inicializa las vistas especificas de la subclase
        initViews(contentView);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        // Navega a la actividad principal si se selecciona el icono de inicio
        if (itemId == R.id.navigation_inicio) {
            navigateToActivity(MainActivity.class);
            return true;
            // Carga el fragmento de reportes dentro de la actividad principal
        } else if (itemId == R.id.navigation_reportes) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment", "reportes");
            startActivity(intent);
            return true;
            // Carga el fragmento de usuarios solo si el usuario es administrador
        } else if (itemId == R.id.navigation_usuarios) {
            if (SessionManager.getInstance().isAdmin()) {
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
        // Controla el boton de retroceso en la toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Metodo de utilidad para navegar entre actividades, evitando duplicados si ya estamos en la actividad actual
    protected void navigateToActivity(Class<?> activityClass) {
        if (this.getClass().equals(activityClass)) {
            return; // No hace nada si ya estamos en la actividad
        }

        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
    // Metodo abstracto que debe retornar el layout especifico de la subclase
    protected abstract int getContentLayoutId();
    // Metodo abstracto para definir el título dinamico de cada subclase
    protected abstract String getTitleForActivity();
    // Metodo abstracto para inicializar vistas dentro de la subclase
    protected abstract void initViews(View contentView);
}
