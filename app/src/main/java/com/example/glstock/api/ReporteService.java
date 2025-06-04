package com.example.glstock.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReporteService {
    //Endpoint genera el pdf de todos los productos
    @GET("api/reportes/procductos/todos")
    Call<ResponseBody> descargarReporteTodos();
    //Endpoint genera el pdf de los 5 productos con menos cantidad (stock)
    @GET("api/reportes/procductos/bajo-stock")
    Call<ResponseBody> descargarReporteBajoStock();
    //Endpoint genera el pdf de todos los productos por nombre de categoria
    @GET("api/reportes/procductos/por-categoria")
    Call<ResponseBody> descargarReportePorCategoria(@Query("categoria") String nombreCategoria);
    //Endpoint genera el pdf de los ultimos 5 productos agregados por fecha ingreso
    @GET("api/reportes/procductos/recientes")
    Call<ResponseBody> descargarReporteProductosRecientes();
    //Endpoint genera el pdf de los ultimos 5 movimientos agregados por fecha ingreso
    @GET("api/reportes/movimientos/recientes")
    Call<ResponseBody> descargarReporteMovimientosRecientes();
    //Endpoint genera el pdf de movimientos por rango de fechas
    @GET("api/reportes/movimientos/por-fechas")
    Call<ResponseBody> descargarReporteMovimientosPorFechas(
            @Query("desde") String desde,
            @Query("hasta") String hasta
    );
}
