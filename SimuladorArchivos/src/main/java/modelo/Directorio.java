package modelo;

import estructuras.ListaEnlazada;

public class Directorio {
    private String nombre;
    private String propietario;
    private ListaEnlazada<Archivo> archivos;
    private ListaEnlazada<Directorio> subdirectorios;

    public Directorio(String nombre) {
        this(nombre, "admin");
    }

    public Directorio(String nombre, String propietario) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.archivos = new ListaEnlazada<>();
        this.subdirectorios = new ListaEnlazada<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public ListaEnlazada<Archivo> getArchivos() { return archivos; }
    public ListaEnlazada<Directorio> getSubdirectorios() { return subdirectorios; }

    public void agregarArchivo(Archivo archivo) {
        this.archivos.agregar(archivo);
    }

    public void agregarSubdirectorio(Directorio dir) {
        this.subdirectorios.agregar(dir);
    }
}