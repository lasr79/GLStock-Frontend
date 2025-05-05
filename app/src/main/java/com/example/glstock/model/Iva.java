package com.example.glstock.model;

import java.io.Serializable;

public class Iva implements Serializable {
    private Long id;
    private Double porcentaje;

    public Iva() {
    }

    public Iva(Long id, Double porcentaje) {
        this.id = id;
        this.porcentaje = porcentaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Double porcentaje) {
        this.porcentaje = porcentaje;
    }
}