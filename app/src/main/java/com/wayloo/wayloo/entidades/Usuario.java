package com.wayloo.wayloo.entidades;


public class Usuario {

    private String nombre;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String calificacion;
    private String NIT;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion= direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String  calificacion) {
        this.calificacion = calificacion;
    }
    public String getNIT() {
        return NIT;
    }

    public void setNit(String  calificacion) {
        this.NIT= calificacion;
    }
}