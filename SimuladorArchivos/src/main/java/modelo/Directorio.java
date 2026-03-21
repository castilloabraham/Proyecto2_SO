package modelo;

import estructuras.ListaEnlazada;

public class Directorio {
    private String nombre;
    private ListaEnlazada<Archivo> archivos;
    private ListaEnlazada<Directorio> subdirectorios;

    public Directorio(String nombre) {
        this.nombre = nombre;
        // Inicializamos tus propias estructuras
        this.archivos = new ListaEnlazada<>();
        this.subdirectorios = new ListaEnlazada<>();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ListaEnlazada<Archivo> getArchivos() {
        return archivos;
    }

    public ListaEnlazada<Directorio> getSubdirectorios() {
        return subdirectorios;
    }
    
    // Método rápido para agregar un archivo a esta carpeta
    public void agregarArchivo(Archivo archivo) {
        this.archivos.agregar(archivo);
    }
    
    // Método rápido para agregar una subcarpeta
    public void agregarSubdirectorio(Directorio dir) {
        this.subdirectorios.agregar(dir);
    }
}