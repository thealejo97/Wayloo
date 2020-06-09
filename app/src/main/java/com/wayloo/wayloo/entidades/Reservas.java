package com.wayloo.wayloo.entidades;


public class Reservas {


    private String fecha_r;
    private String HI_r;
    private String HF_r;
    private String nombre_barbero_r;
    private String id_barbero;


    public String getFecha_r() {
        return fecha_r;
    }

    public void setFecha_r(String FR) {
        this.fecha_r = FR;
    }

    public String getHI_r() {
        return HI_r;
    }

    public void setHI_r(String HI) {
        this.HI_r = HI;
    }

    public String getHF_r() {
        return HF_r;
    }

    public void setHF_r(String HF) {
        this.HF_r= HF;
    }

    public String getNombre_barbero_r() {
        return nombre_barbero_r;
    }

    public void setNombre_barbero_r(String name) {
        this.nombre_barbero_r = name;
    }
    public String getId_barbero() {
        return id_barbero;
    }

    public void setId_barbero(String name) {
        this.id_barbero = name;
    }


}