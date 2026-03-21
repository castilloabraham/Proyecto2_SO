package controlador;

import modelo.Archivo;
import modelo.Bloque;
import modelo.Directorio;
import modelo.Disco;

public class GestorArchivos {
    
    private Disco disco;
    private Directorio directorioRaiz;

    public GestorArchivos() {
        this.disco = new Disco(100);
        this.directorioRaiz = new Directorio("raiz");
    }

    // MÉTODOS EXISTENTES
    public Disco getDisco() { return disco; }
    public Directorio getDirectorioRaiz() { return directorioRaiz; }

    // --- NUEVO: LÓGICA PARA CREAR ARCHIVO ---
    // Retorna true si se pudo crear, false si no hay espacio
    public boolean crearArchivo(String nombre, int tamaño, String propietario) {
        // 1. Verificar si hay espacio suficiente
        if (disco.obtenerEspacioLibre() < tamaño) {
            return false; 
        }

        // 2. Buscar bloques libres y ocuparlos
        int bloquesAsignados = 0;
        int primerBloque = -1;
        Bloque[] bloquesReales = disco.getBloques();

        for (int i = 0; i < disco.getCapacidad(); i++) {
            if (bloquesReales[i].isLibre()) {
                if (bloquesAsignados == 0) {
                    primerBloque = i; // Guardamos dónde empieza el archivo
                }
                bloquesReales[i].setLibre(false); // Lo marcamos como ocupado
                bloquesAsignados++;
            }
            if (bloquesAsignados == tamaño) {
                break; // Ya encontramos todo el espacio necesario
            }
        }

        // 3. Crear el archivo lógicamente y meterlo en la carpeta raíz
        Archivo nuevoArchivo = new Archivo(nombre, tamaño, primerBloque, propietario);
        directorioRaiz.agregarArchivo(nuevoArchivo);

        return true; // Éxito
    }
}