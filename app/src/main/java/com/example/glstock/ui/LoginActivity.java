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
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Comprobar si ya hay una sesión activa
        if (SessionManager.getInstance().isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Inicializar el servicio de autenticación
        authService = ApiClient.getClient().create(AuthService.class);

        // Configurar botón de inicio de sesión
        binding.btnEntrar.setOnClickListener(v -> attemptLogin());
        binding.ivArrow.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        // Obtener valores de los campos
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validar campos
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("El correo es obligatorio");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("La contraseña es obligatoria");
            return;
        }

        // Mostrar progreso
        showProgress(true);

        // Crear mapa de datos para la solicitud
        Map<String, String> loginData = new HashMap<>();
        loginData.put("correo", email);
        loginData.put("contrasena", password);

        // Realizar la llamada a la API
        authService.login(loginData).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Guardar datos de sesión
                    SessionManager.getInstance().saveAuthData(
                            loginResponse.getToken(),
                            loginResponse.getCorreo(),
                            loginResponse.getRol()
                    );

                    // Navegar a la pantalla principal
                    navigateToMainActivity();
                } else {
                    // Mostrar error
                    Toast.makeText(LoginActivity.this,
                            "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerrar esta actividad para que no se pueda volver atrás
    }

    private void showProgress(boolean show) {
        binding.btnEntrar.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
        binding.etPassword.setEnabled(!show);
        // Aquí podrías agregar un ProgressBar y configurarlo como visible/invisible
    }
}