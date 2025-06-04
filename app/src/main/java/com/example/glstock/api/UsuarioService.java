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
    //Endpoint crea un nuevo usuario
    @POST("api/usuarios/crear")
    Call<Usuario> crearUsuario(@Body Usuario usuario);
    //Endpoint actualiza la informacion del usuario
    @PUT("api/usuarios/actualizar/{id}")
    Call<Usuario> actualizarUsuario(@Path("id") Long id, @Body Usuario usuario);
    //Endpoint elimina la informacion del usuario
    @DELETE("api/usuarios/eliminar/{id}")
    Call<Void> eliminarUsuario(@Path("id") Long id);
    //Endpoint busca la informacion del usuario por el nombre
    @GET("api/usuarios/buscar-nombre")
    Call<List<Usuario>> buscarUsuariosPorNombre(@Query("nombre") String nombre);
    //Endpoint busca la informacion del usuario por el correo
    @GET("api/usuarios/buscar-correo")
    Call<List<Usuario>> buscarPorCorreo(@Query("correo") String correo);
}