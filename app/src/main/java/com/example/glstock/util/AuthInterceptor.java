package com.example.glstock.util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Omitir agregar el token para la solicitud de inicio de sesión
        if (originalRequest.url().toString().contains("api/auth/login")) {
            return chain.proceed(originalRequest);
        }

        String token = SessionManager.getInstance().getToken();

        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}