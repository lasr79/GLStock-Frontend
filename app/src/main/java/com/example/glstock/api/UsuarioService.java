package com.example.glstock.api;

import com.example.glstock.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioService {
    @POST("api/usuarios")
    Call<Usuario> crearUsuario(@Body Usuario usuario);

    @PUT("api/usuarios/{id}")
    Call<Usuario> actualizarUsuario(@Path("id") Long id, @Body Usuario usuario);

    @DELETE("api/usuarios/{id}")
    Call<Void> eliminarUsuario(@Path("id") Long id);

    @GET("api/usuarios")
    Call<List<Usuario>> buscarUsuariosPorNombre(@Query("nombre") String nombre);
}