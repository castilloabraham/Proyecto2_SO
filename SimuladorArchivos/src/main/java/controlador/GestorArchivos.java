package controlador;

import modelo.Directorio;
import modelo.Disco;

public class GestorArchivos {
    
    private Disco disco;
    private Directorio directorioRaiz;

    public GestorArchivos() {
        // Inicializamos el disco duro simulado con 100 bloques (puedes cambiar este número luego)
        this.disco = new Disco(100);
        
        // Creamos la carpeta principal de todo el sistema
        this.directorioRaiz = new Directorio("raiz");
    }

    // Métodos para que la interfaz pueda obtener los datos reales
    public Disco getDisco() {
        return disco;
    }

    public Directorio getDirectorioRaiz() {
        return directorioRaiz;
    }
}