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
    
    public boolean eliminarArchivo(String nombre) {
        estructuras.ListaEnlazada<modelo.Archivo> archivos = directorioRaiz.getArchivos();
        
        // 1. Buscamos el archivo por su nombre en la lista
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            
            if (arch.getNombre().equals(nombre)) {
                // 2. Liberamos los bloques en el disco (volverlos a gris)
                Bloque[] bloquesReales = disco.getBloques();
                int inicio = arch.getBloqueInicial();
                int tamano = arch.getTamañoEnBloques();
                
                // Recorremos los bloques que ocupaba y los marcamos como libres
                int bloquesLiberados = 0;
                for (int j = inicio; bloquesLiberados < tamano && j < disco.getCapacidad(); j++) {
                    if (!bloquesReales[j].isLibre()) {
                        bloquesReales[j].setLibre(true);
                        bloquesLiberados++;
                    }
                }
                
                // 3. Lo borramos de la carpeta (nuestra ListaEnlazada)
                archivos.eliminar(i);
                return true; // Éxito
            }
        }
        return false; // No se encontró el archivo
    }
    
    public boolean crearDirectorio(String nombre) {
        // Por ahora, crearemos las carpetas directamente dentro de "raiz"
        Directorio nuevoDir = new Directorio(nombre);
        directorioRaiz.agregarSubdirectorio(nuevoDir);
        return true;
    }
}