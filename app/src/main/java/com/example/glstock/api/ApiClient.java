package com.example.glstock.api;
import com.example.glstock.util.AuthInterceptor;
import com.google.gson.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor de logs
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            httpClient.addInterceptor(new AuthInterceptor());

            // Configuración de Gson para fechas
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                            (json, type, context) -> LocalDate.parse(json.getAsJsonPrimitive().getAsString()))
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                            (json, type, context) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .registerTypeAdapter(Date.class, (JsonDeserializer<Date>)
                            (json, type, context) -> Date.valueOf(json.getAsString()))
                    .registerTypeAdapter(Date.class, (JsonSerializer<Date>)
                            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString())) // <-- ESTO ES CLAVE
                    .create();

            // Construir Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(new NullOnEmptyConverterFactory()) // 👈 para evitar errores por cuerpo vacío
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    // Clase para manejar respuestas vacías
    public static class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return (ResponseBody body) -> {
                if (body.contentLength() == 0) return null;
                return delegate.convert(body);
            };
        }
    }
}