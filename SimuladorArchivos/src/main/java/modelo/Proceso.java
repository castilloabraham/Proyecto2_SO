package modelo;

public class Proceso extends Thread {
    private String idProceso;
    private String estado; // "Nuevo", "Listo", "Ejecutando", "Bloqueado", "Terminado"
    private String tipoOperacion; // "CREAR", "LEER", "ACTUALIZAR", "ELIMINAR"
    private String nombreArchivo;
    private int bloqueDestino;

    public Proceso(String idProceso, String tipoOperacion, String nombreArchivo, int bloqueDestino) {
        this.idProceso = idProceso;
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.bloqueDestino = bloqueDestino;
        this.estado = "Nuevo"; // Estado inicial exigido
    }

    // --- Getters y Setters ---
    public String getIdProceso() { return idProceso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getBloqueDestino() { return bloqueDestino; }

    @Override
    public void run() {
        // Aquí es donde simularemos el tiempo que tarda el disco
        try {
            this.estado = "Ejecutando";
            // Simula el tiempo de viaje de la cabeza del disco (luego lo conectaremos al slider)
            Thread.sleep(500); 
            this.estado = "Terminado";
        } catch (InterruptedException e) {
            this.estado = "Bloqueado";
        }
    }
    
    @Override
    public String toString() {
        return idProceso + " [" + tipoOperacion + " " + nombreArchivo + "] - " + estado;
    }
}