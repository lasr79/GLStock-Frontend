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
    //Endpoint registro de movimiento
    @POST("api/movimientos/registrar")
    Call<Movimiento> registrarMovimiento(@Body Movimiento movimiento);
    //Endpoint busqueda ultimos 10 movimientos por fecha de ingreso
    @GET("api/movimientos/ultimos-10-movimientos")
    Call<List<Movimiento>> ultimos10Movimientos();
    //Endpoint busqueda de movimientos por rangos
    @GET("api/movimientos/rango")
    Call<List<Movimiento>> movimientosPorRango(
            @Query("inicio") LocalDateTime inicio,
            @Query("fin") LocalDateTime fin);
}