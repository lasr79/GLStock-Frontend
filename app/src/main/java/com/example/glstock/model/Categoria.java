package com.example.glstock.model;
import java.io.Serializable;

public class Categoria implements Serializable {
    private Long id;
    private String nombre;
    private Iva iva;

    public Categoria() {
    }

    public Categoria(Long id, String nombre, Iva iva) {
        this.id = id;
        this.nombre = nombre;
        this.iva = iva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Iva getIva() {
        return iva;
    }

    public void setIva(Iva iva) {
        this.iva = iva;
    }
}