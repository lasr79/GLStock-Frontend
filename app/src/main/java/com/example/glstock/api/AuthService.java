package com.example.glstock.api;

import com.example.glstock.model.LoginResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    //Endpoint de login
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body Map<String, String> loginData);
}