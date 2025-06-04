package com.example.glstock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glstock.R;
import com.example.glstock.api.ApiClient;
import com.example.glstock.api.AuthService;
import com.example.glstock.databinding.ActivityLoginBinding;
import com.example.glstock.model.LoginResponse;
import com.example.glstock.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    // Servicio de autenticacion para enviar la solicitud de login
    private AuthService authService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Comprobar si ya hay una sesion activa; si es así va a la pantalla principal
        if (SessionManager.getInstance().isLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        // Inicializar el servicio de autenticacion usando la api
        authService = ApiClient.getClient().create(AuthService.class);
        // Configurar el boton para intentar iniciar sesion
        binding.btnEntrar.setOnClickListener(v -> attemptLogin());
    }
    // Metodo para validar campos e intentar el login
    private void attemptLogin() {
        // Obtener valores de los campos de entrada
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        // Validacion: el correo no puede estar vacio
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("El correo es obligatorio");
            return;
        }
        // Validacion: la contraseña no puede estar vacia
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("La contraseña es obligatoria");
            return;
        }
        showProgress(true);
        // Preparar datos para el body de la solicitud
        Map<String, String> loginData = new HashMap<>();
        loginData.put("correo", email);
        loginData.put("contrasena", password);
        // Llamada al endpoint de login
        authService.login(loginData).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Guardar token, correo y rol en SessionManager
                    SessionManager.getInstance().saveAuthData(
                            loginResponse.getToken(),
                            loginResponse.getCorreo(),
                            loginResponse.getRol()
                    );
                    // Ir a la pantalla principal
                    navigateToMainActivity();
                } else {
                    // Si las credenciales no son validas muestra mensaje
                    Toast.makeText(LoginActivity.this,
                            "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showProgress(false);
                // Error de red o del servidor
                Toast.makeText(LoginActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Metodo para cambiar a la pantalla principal y cerrar la actual
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cierra LoginActivity para que no se pueda volver con el botón atrás
    }

    // Metodo auxiliar para mostrar/ocultar progreso deshabilitando inputs
    private void showProgress(boolean show) {
        binding.btnEntrar.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
        binding.etPassword.setEnabled(!show);
    }
}
