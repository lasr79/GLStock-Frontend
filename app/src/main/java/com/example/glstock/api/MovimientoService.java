package com.example.glstock.api;

import com.example.glstock.model.Movimiento;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MovimientoService {
    @POST("api/movimientos")
    Call<Movimiento> registrarMovimiento(@Body Movimiento movimiento);

    @GET("api/movimientos/ultimos-10-dias")
    Call<List<Movimiento>> movimientosUltimos10Dias();

    @GET("api/movimientos/rango")
    Call<List<Movimiento>> movimientosPorRango(
            @Query("inicio") LocalDateTime inicio,
            @Query("fin") LocalDateTime fin);
}