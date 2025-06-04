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
    //Endpoint busqueda de todos producyos por nombre
    @GET("api/productos/buscar")
    Call<List<Producto>> buscarPorNombre(@Query("nombre") String nombre);
    //Endpoint busqueda de catalogos  por id
    @POST("api/productos/categoria")
    Call<List<Producto>> buscarPorCategoria(@Body Map<String, Long> datos);
    //Endpoint busqueda de catalogos por nombre
    @GET("api/productos/buscar-categoria")
    Call<List<Producto>> buscarPorNombreCategoria(@Query("nombre") String nombreCategoria);
    //Endpoint busqueda de los 5 productos con menor cantidad (stock)
    @GET("api/productos/stock-menor")
    Call<List<Producto>> obtenerProductosConMenorStock();
    //Endpoint busqueda de los 5 productos con menor cantidad (stock)
    @GET("api/productos/recientes")
    Call<List<Producto>> productosRecientes();
    //Endpoint crear producto
    @POST("api/productos/crear")
    Call<Producto> crearProducto(@Body Producto producto);
    //Endpoint actualizacion del producto
    @PUT("api/productos/actualizar/{id}")
    Call<Producto> actualizarProducto(@Path("id") Long id, @Body Producto producto);
    //Endpoint Elimina producto por id
    @DELETE("api/productos/eliminar/{id}")
    Call<Void> eliminarProducto(@Path("id") Long id);
    //Endpoint busqueda de todos los producto
    @GET("api/productos/todos")
    Call<List<Producto>> obtenerTodosLosProductos();
}