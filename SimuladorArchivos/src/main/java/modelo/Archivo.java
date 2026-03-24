package modelo;

public class Archivo {
    private String nombre;
    private int tamañoEnBloques;
    private int bloqueInicial;
    private String propietario;

    private int lectoresActivos;
    private boolean siendoEscrito;

    public Archivo(String nombre, int tamañoEnBloques, int bloqueInicial, String propietario) {
        this.nombre = nombre;
        this.tamañoEnBloques = tamañoEnBloques;
        this.bloqueInicial = bloqueInicial;
        this.propietario = propietario;
        
        this.lectoresActivos = 0;
        this.siendoEscrito = false;
    }

    // --- Getters y Setters normales ---
    public String getNombre() { return nombre; }
    public int getTamañoEnBloques() { return tamañoEnBloques; }
    public int getBloqueInicial() { return bloqueInicial; }
    public String getPropietario() { return propietario; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // --- NUEVO: Lógica de Locks (Lectores/Escritores) ---
    public synchronized boolean intentarLeer() {
        if (siendoEscrito) {
            return false; // No puede leer si alguien está escribiendo
        }
        lectoresActivos++; // Lock compartido
        return true;
    }

    public synchronized void terminarLeer() {
        if (lectoresActivos > 0) {
            lectoresActivos--; // Libera su lock de lectura
        }
    }

    public synchronized boolean intentarEscribir() {
        if (siendoEscrito || lectoresActivos > 0) {
            return false; // No puede escribir si ya hay alguien escribiendo o leyendo
        }
        siendoEscrito = true; // Lock exclusivo
        return true;
    }

    public synchronized void terminarEscribir() {
        siendoEscrito = false; // Libera el lock exclusivo
    }
    
    public boolean isSiendoEscrito() {
        return siendoEscrito;
    }

    public int getLectoresActivos() {
        return lectoresActivos;
    }
}