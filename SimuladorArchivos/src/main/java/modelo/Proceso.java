package modelo;

public class Proceso {
    private String idProceso;
    private String estado; 
    private String tipoOperacion; 
    private String nombreArchivo;
    private int bloqueDestino;
    private int tamano; 

    // Constructor actualizado
    public Proceso(String idProceso, String tipoOperacion, String nombreArchivo, int bloqueDestino, int tamano) {
        this.idProceso = idProceso;
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.bloqueDestino = bloqueDestino;
        this.tamano = tamano;
        this.estado = "Nuevo"; 
    }

    // --- Getters y Setters ---
    public String getIdProceso() { return idProceso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getBloqueDestino() { return bloqueDestino; }
    
    public int getTamano() { return tamano; } // <-- NUEVO GETTER

    @Override
    public String toString() {
        return idProceso + " [" + tipoOperacion + " " + nombreArchivo + "] - " + estado;
    }
}