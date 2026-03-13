package modelo;

import estructuras.ListaEnlazada;

public class Archivo {
    private String nombre;
    private String dueno;
    private int tamanoBloques;
    // Aquí cumplimos con la asignación encadenada usando tu propia estructura
    private ListaEnlazada<Bloque> bloquesAsignados; 

    public Archivo(String nombre, String dueno, int tamanoBloques) {
        this.nombre = nombre;
        this.dueno = dueno;
        this.tamanoBloques = tamanoBloques;
        this.bloquesAsignados = new ListaEnlazada<>();
    }

    // Getters y Setters básicos
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDueno() { return dueno; }
    public void setDueno(String dueno) { this.dueno = dueno; }

    public int getTamanoBloques() { return tamanoBloques; }

    public ListaEnlazada<Bloque> getBloquesAsignados() { return bloquesAsignados; }
}