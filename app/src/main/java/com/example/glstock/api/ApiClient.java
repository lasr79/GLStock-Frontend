package com.example.glstock.api;

import com.example.glstock.util.AuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Esta URL apunta al localhost de tu computadora desde el emulador
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Crear interceptor de registro
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Crear OkHttpClient con nuestros interceptores
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            httpClient.addInterceptor(new AuthInterceptor());

            // Configurar Gson para manejar LocalDate y LocalDateTime
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                            (json, type, jsonDeserializationContext) ->
                                    LocalDate.parse(json.getAsJsonPrimitive().getAsString()))
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                            (json, type, jsonDeserializationContext) ->
                                    LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .create();

            // Construir instancia de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}