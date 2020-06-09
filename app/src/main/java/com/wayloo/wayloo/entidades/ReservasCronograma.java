package com.wayloo.wayloo.entidades;


public class ReservasCronograma {


    private String fecha_r;
    private String HI_r;
    private String HF_r;
    private String nombre_cliente;
    private String id_cliente;


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

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public void setNombre_cliente(String name) {
        this.nombre_cliente = name;
    }
    public String getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(String name) {
        this.id_cliente = name;
    }


}