package com.example.glstock.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.auth0.android.jwt.JWT;

public class SessionManager {
    private static final String PREF_NAME = "GLStockPrefs";
    private static final String KEY_TOKEN = "authToken";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private static SessionManager instance;
    private SharedPreferences preferences;

    private SessionManager() {
        // Constructor privado para singleton
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveAuthData(String token, String email, String role) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public void clearAuthData() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_ROLE)
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty() && !isTokenExpired(token);
    }

    public boolean isAdmin() {
        String role = getUserRole();
        return role != null && role.equals("ADMIN");
    }

    private boolean isTokenExpired(String token) {
        try {
            JWT jwt = new JWT(token);
            return jwt.isExpired(0); // 0 significa sin margen de error
        } catch (Exception e) {
            return true; // Si hay un error al analizar el token, considerarlo expirado
        }
    }
}