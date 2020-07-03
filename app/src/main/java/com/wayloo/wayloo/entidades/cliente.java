package com.wayloo.wayloo.entidades;

public class cliente {

    private String telefono;
    private String nombre;
    private String apellido;
    public  String fireB;

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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido= apellido;
    }

    public void setFireB(String img){
        this.fireB = img;
    }

    public String getFireB() {
        return fireB;
    }

}
