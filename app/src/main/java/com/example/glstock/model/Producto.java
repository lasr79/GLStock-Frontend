package com.example.glstock.model;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

public class Producto implements Serializable {
    private Long id;
    private String nombre;
    private String descripcion;
    private Categoria categoria;
    private Double precio;
    private Integer cantidad;
    private java.sql.Date fechaIngreso;
    private String urlImagen;

    public Producto() {
    }

    public Producto(Long id, String nombre, String descripcion, Categoria categoria,
                    Double precio, Integer cantidad, java.sql.Date fechaIngreso, String urlImagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
        this.fechaIngreso = fechaIngreso;
        this.urlImagen = urlImagen;
    }

    // Constructor alternativo que acepta LocalDate
    public Producto(Long id, String nombre, String descripcion, Categoria categoria,
                    Double precio, Integer cantidad, LocalDate fechaIngreso, String urlImagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
        this.fechaIngreso = fechaIngreso != null ? Date.valueOf(String.valueOf(fechaIngreso)) : null;
        this.urlImagen = urlImagen;
    }

    // Getters y setters
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public java.sql.Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(java.sql.Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    // Método adicional para aceptar LocalDate
    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso != null ? Date.valueOf(String.valueOf(fechaIngreso)) : null;
    }

    // Método para obtener la fecha como LocalDate
    public LocalDate getFechaIngresoAsLocalDate() {
        return fechaIngreso != null ? LocalDate.parse(fechaIngreso.toString()) : null;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
}