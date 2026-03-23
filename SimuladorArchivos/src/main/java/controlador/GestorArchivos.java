package controlador;

import modelo.Archivo;
import modelo.Bloque;
import modelo.Directorio;
import modelo.Disco;
import estructuras.Cola;
import modelo.Proceso;

public class GestorArchivos {
    
    private Disco disco;
    private Directorio directorioRaiz;
    
    private Cola<Proceso> colaProcesos; 
    private int contadorProcesos = 1;
    private Planificador planificador;
    
    private interfaz.VentanaPrincipal ventana;

    public GestorArchivos() {
        this.disco = new Disco(100);
        this.directorioRaiz = new Directorio("raiz");
        this.colaProcesos = new Cola<>();
        this.planificador = new Planificador(this);
        this.planificador.start(); // ¡Esto enciende el motor en segundo plano!
    }
    
    public void setVentana(interfaz.VentanaPrincipal ventana) {
        this.ventana = ventana;
    }

    public void imprimirEnLogVisual(String mensaje) {
        if (this.ventana != null) {
            this.ventana.agregarMensajeLog(mensaje);
        }
    }

    public void actualizarColaVisual() {
        if (this.ventana != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== COLA DE PROCESOS (I/O) ===\n");
            sb.append("Esperando para usar el disco: ").append(colaProcesos.getTamano()).append(" procesos.\n");
            this.ventana.actualizarPantallaProcesos(sb.toString());
        }
    }
    
    public Cola<Proceso> getColaProcesos() { return colaProcesos; }
    public Disco getDisco() { return disco; }
    public Directorio getDirectorioRaiz() { return directorioRaiz; }
    private int posicionCabeza = 0; // La aguja siempre empieza en el bloque 0

    // 3. Asegúrate de tener el getter y setter para la aguja del disco:
    public int getPosicionCabeza() { return posicionCabeza; }
    public void setPosicionCabeza(int posicion) { this.posicionCabeza = posicion; }
    
    public void encolarSolicitudLectura(String nombreArchivo) {
        // En un proyecto real, buscaríamos el bloqueInicial del archivo primero
        Proceso p = new Proceso("P" + contadorProcesos++, "LEER", nombreArchivo, 0);
        p.setEstado("Listo");
        colaProcesos.encolar(p);
    }

    // Retorna true si se pudo crear, false si no hay espacio
    public boolean crearArchivo(String nombre, int tamaño, String propietario) {
        // 1. Verificar si hay espacio suficiente
        if (disco.obtenerEspacioLibre() < tamaño) {
            return false; 
        }

        // 2. Buscar bloques libres y ocuparlos (CON ASIGNACIÓN ENCADENADA)
        int bloquesAsignados = 0;
        int primerBloque = -1;
        int bloqueAnterior = -1; // <-- NUEVO: Para llevar el rastro y enlazar
        Bloque[] bloquesReales = disco.getBloques();

        for (int i = 0; i < disco.getCapacidad(); i++) {
            if (bloquesReales[i].isLibre()) {
                
                if (bloquesAsignados == 0) {
                    primerBloque = i; // Guardamos dónde empieza el archivo
                } else {
                    // NUEVO: Le decimos al bloque anterior que apunte a este nuevo bloque
                    bloquesReales[bloqueAnterior].setSiguienteBloque(i);
                }
                
                bloquesReales[i].setLibre(false); // Lo marcamos como ocupado
                bloquesReales[i].setArchivoAsignado(nombre); // Le damos el nombre para el tooltip
                bloquesReales[i].setContenido("Datos de: " + nombre); 
                bloquesReales[i].setSiguienteBloque(-1); // <-- NUEVO: Por defecto apunta a -1 (Fin de archivo)
                
                bloqueAnterior = i; // Actualizamos el bloque anterior para la próxima iteración
                bloquesAsignados++;
            }
            
            // Si ya encontramos todo el espacio necesario, nos salimos del ciclo
            if (bloquesAsignados == tamaño) {
                break; 
            }
        }

        // 3. Crear el archivo lógicamente y meterlo en la carpeta raíz
        Archivo nuevoArchivo = new Archivo(nombre, tamaño, primerBloque, propietario);
        directorioRaiz.agregarArchivo(nuevoArchivo);

        return true; // Éxito
    }
    
    public boolean eliminarArchivo(String nombre) {
        estructuras.ListaEnlazada<modelo.Archivo> archivos = directorioRaiz.getArchivos();
        
        // 1. Buscamos el archivo por su nombre en la lista de la carpeta raíz
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            
            if (arch.getNombre().equals(nombre)) {
                // 2. Liberamos los bloques encadenados en el disco
                Bloque[] bloquesReales = disco.getBloques();
                
                // Empezamos por la "cabeza" del archivo (el primer bloque)
                int bloqueActual = arch.getBloqueInicial();
                
                // Recorremos la cadena hasta que encontremos el fin de archivo (-1)
                while (bloqueActual != -1) {
                    // Guardamos quién es el siguiente ANTES de borrar el actual
                    int siguienteBloque = bloquesReales[bloqueActual].getSiguienteBloque();
                    
                    // "Limpiamos" el bloque actual y lo volvemos a poner gris
                    bloquesReales[bloqueActual].setLibre(true);
                    bloquesReales[bloqueActual].setArchivoAsignado("Ninguno"); 
                    bloquesReales[bloqueActual].setContenido("");
                    bloquesReales[bloqueActual].setSiguienteBloque(-1); // Reseteamos el apuntador
                    
                    // Saltamos al siguiente eslabón de la cadena
                    bloqueActual = siguienteBloque;
                }
                
                // 3. Finalmente, lo borramos de la carpeta (nuestra ListaEnlazada)
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
                
                // 1. Creamos un proceso simulando la solicitud de I/O
                modelo.Proceso p = new modelo.Proceso("P" + contadorProcesos++, "LEER", nombre, bloqueDestino);
                p.setEstado("Listo"); // Estado inicial antes de entrar al disco
                
                // 2. Lo metemos en la cola de procesos para que el Planificador lo atienda
                colaProcesos.encolar(p);
                actualizarColaVisual();
                
                // 3. Devolvemos un mensaje indicando que la solicitud está en espera
                return "⏳ Solicitud enviada a la cola de I/O.\n" +
                       "El disco se moverá pronto hacia el bloque " + bloqueDestino + ".\n" +
                       "Revisa el Log de Procesos.";
            }
        }
        return null; // Retorna null si no encontró el archivo
    }
    public void cambiarVelocidadDisco(int nuevaVelocidad) {
        if (this.planificador != null) {
            this.planificador.setVelocidad(nuevaVelocidad);
        }
    }

    public void cambiarPoliticaPlanificador(String nuevaPolitica) {
        if (this.planificador != null) {
            this.planificador.setPolitica(nuevaPolitica);
        }
    }
}