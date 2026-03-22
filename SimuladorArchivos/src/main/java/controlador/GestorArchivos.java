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

    public Disco getDisco() { return disco; }
    public Directorio getDirectorioRaiz() { return directorioRaiz; }
    private int posicionCabeza = 0; // La aguja siempre empieza en el bloque 0

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
                bloquesReales[i].setArchivoAsignado(nombre); // Le damos el nombre para el tooltip
                bloquesReales[i].setContenido("Datos de: " + nombre); 
                
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
                        bloquesReales[j].setArchivoAsignado("Ninguno"); // Borramos el nombre para el tooltip
                        bloquesReales[j].setContenido("");
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
    
    public boolean renombrarItem(String nombreAntiguo, String nombreNuevo) {
        // 1. Primero buscamos si es un archivo
        estructuras.ListaEnlazada<modelo.Archivo> archivos = directorioRaiz.getArchivos();
        for (int i = 0; i < archivos.getTamano(); i++) {
            if (archivos.obtener(i).getNombre().equals(nombreAntiguo)) {
                archivos.obtener(i).setNombre(nombreNuevo);
                return true; // Éxito
            }
        }
        
        // 2. Si no era un archivo, buscamos si es una carpeta (directorio)
        estructuras.ListaEnlazada<modelo.Directorio> subdirs = directorioRaiz.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            if (subdirs.obtener(i).getNombre().equals(nombreAntiguo)) {
                subdirs.obtener(i).setNombre(nombreNuevo);
                return true; // Éxito
            }
        }
        
        return false; // No se encontró ni archivo ni carpeta con ese nombre
    }
    
    public String obtenerEstadisticas() {
        int total = disco.getCapacidad();
        int libre = disco.obtenerEspacioLibre();
        int ocupado = total - libre;
        
        // Contamos lo que hay en la raíz
        int numArchivos = directorioRaiz.getArchivos().getTamano();
        int numCarpetas = directorioRaiz.getSubdirectorios().getTamano();

        // Calculamos el porcentaje de uso
        double porcentajeUso = ((double) ocupado / total) * 100;

        // Armamos un texto bonito para mostrar
        return "📊 ESTADÍSTICAS DEL DISCO 📊\n\n" +
               "🔹 Capacidad Total: " + total + " bloques\n" +
               "🔴 Espacio Ocupado: " + ocupado + " bloques (" + String.format("%.1f", porcentajeUso) + "%)\n" +
               "🟢 Espacio Libre: " + libre + " bloques\n" +
               "📄 Total de Archivos: " + numArchivos + "\n" +
               "📁 Total de Carpetas: " + numCarpetas;
    }
    
    public String leerArchivo(String nombre) {
        estructuras.ListaEnlazada<modelo.Archivo> archivos = directorioRaiz.getArchivos();
        
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            
            if (arch.getNombre().equals(nombre)) {
                int bloqueDestino = arch.getBloqueInicial();
                
                // Calculamos cuánto tuvo que viajar la cabeza del disco (matemática de valor absoluto)
                int movimiento = Math.abs(bloqueDestino - posicionCabeza);
                
                // Movemos la cabeza hasta el final del archivo que acabamos de leer
                posicionCabeza = bloqueDestino + arch.getTamañoEnBloques() - 1; 
                
                return "✅ Archivo '" + nombre + "' leído exitosamente.\n\n" +
                       "📍 La cabeza viajó " + movimiento + " bloques para encontrarlo.\n" +
                       "🎯 Posición actual de la cabeza: Bloque " + posicionCabeza;
            }
        }
        return null; // Retorna null si no encontró el archivo
    }
}