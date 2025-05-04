package com.example.glstock.api;

import com.example.glstock.model.Producto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductoService {
    @GET("api/productos/buscar")
    Call<List<Producto>> buscarPorNombre(@Query("nombre") String nombre);

    @POST("api/productos/categoria")
    Call<List<Producto>> buscarPorCategoria(@Body Map<String, Long> datos);

    @POST("api/productos/menor-stock")
    Call<List<Producto>> productosMenorStock(@Body Map<String, Object> datos);

    @GET("api/productos/stock-menor")
    Call<List<Producto>> obtenerProductosConMenorStock();

    @GET("api/productos/recientes")
    Call<List<Producto>> productosRecientes();

    @POST("api/productos/crear")
    Call<Producto> crearProducto(@Body Producto producto);

    @PUT("api/productos/actualizar/{id}")
    Call<Producto> actualizarProducto(@Path("id") Long id, @Body Producto producto);

    @DELETE("api/productos/eliminar/{id}")
    Call<Void> eliminarProducto(@Path("id") Long id);

    @GET("api/productos/buscar-id/{id}")
    Call<Producto> buscarPorId(@Path("id") Long id);
}