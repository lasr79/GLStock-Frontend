package com.example.glstock.api;

import com.example.glstock.model.Categoria;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoriaService {
    @GET("api/categorias/listar")
    Call<List<Categoria>> listarCategorias();
}