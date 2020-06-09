package com.wayloo.wayloo.entidades;

public class BarberosU {


    private String fireB;
    private String nombre_P;
    private String apellido_P;
    private String telefono_P;
    private String h_inicio;
    private String h_fin;
    private String calificacion;
    private String NIT_pertenese;


    public String getNombre() {
        return nombre_P;
    }


    public void setNombre(String nombre) {
        this.nombre_P = nombre;
    }

    public String getTelefono() {
        return telefono_P;
    }

    public void setTelefono(String telefono) {
        this.telefono_P = telefono;
    }

    public String getApellido_P() {
        return apellido_P;
    }

    public void setApellido_P(String direccion) {
        this.apellido_P= direccion;
    }

    public String geth_inicio() {
        return h_inicio;
    }

    public void seth_inicio(String ciudad) {
        this.h_inicio = ciudad;
    }

    public String geth_fin() {
        return h_fin;
    }

    public void seth_fin(String ciudad) {
        this.h_fin = ciudad;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String  calificacion) {
        this.calificacion = calificacion;
    }

    public String getNIT_pertenese() {
        return NIT_pertenese;
    }

    public void setNIT_pertenese(String  nit) {
        this.NIT_pertenese = nit;
    }

    public void setFireB(String img){
        this.fireB = img;
    }

    public String getFireB() {
        return fireB;
    }
}
