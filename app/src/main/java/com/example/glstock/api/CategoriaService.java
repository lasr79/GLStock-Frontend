package com.example.glstock.api;

import com.example.glstock.model.Categoria;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CategoriaService {
    //Endpoint busqueda de todos los catalogos
    @GET("api/categorias/listar")
    Call<List<Categoria>> listarCategorias();
    //Endpoint crea un catalogo nuevo
    @POST("api/categorias/crear")
    Call<Categoria> crearCategoria(@Body Categoria categoria);
}