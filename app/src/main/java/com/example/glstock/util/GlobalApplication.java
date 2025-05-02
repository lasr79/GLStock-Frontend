package com.example.glstock.util;

import android.app.Application;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar SessionManager con el contexto de la aplicación
        SessionManager.getInstance().init(getApplicationContext());
    }
}