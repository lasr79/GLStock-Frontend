package com.example.glstock.model;

import java.time.LocalDateTime;

public class Movimiento {
    private Long id;
    private Producto producto;
    private Usuario usuario;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private String motivo;
    private LocalDateTime fecha;

    public Movimiento() {
    }

    public Movimiento(Long id, Producto producto, Usuario usuario, TipoMovimiento tipo,
                      Integer cantidad, String motivo, LocalDateTime fecha) {
        this.id = id;
        this.producto = producto;
        this.usuario = usuario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}