package modelo;

public class Proceso {
    private String idProceso;
    private String estado;
    private String tipoOperacion;
    private String nombreArchivo;
    private String nombreNuevo;
    private int bloqueDestino;
    private int tamano;
    private String propietarioSolicitante;
    private boolean directorioObjetivo;
    private String directorioPadre;
    private boolean esperandoLock;
    private int reintentosLock;

    public Proceso(String idProceso, String tipoOperacion, String nombreArchivo, int bloqueDestino, int tamano) {
        this.idProceso = idProceso;
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.bloqueDestino = bloqueDestino;
        this.tamano = tamano;
        this.estado = "Nuevo";
        this.nombreNuevo = "";
        this.propietarioSolicitante = "admin";
        this.directorioObjetivo = false;
        this.directorioPadre = "raiz";
        this.esperandoLock = false;
        this.reintentosLock = 0;
    }

    public String getIdProceso() { return idProceso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }

    public String getNombreNuevo() { return nombreNuevo; }
    public void setNombreNuevo(String nombreNuevo) { this.nombreNuevo = nombreNuevo; }

    public int getBloqueDestino() { return bloqueDestino; }
    public void setBloqueDestino(int bloqueDestino) { this.bloqueDestino = bloqueDestino; }

    public int getTamano() { return tamano; }

    public String getPropietarioSolicitante() { return propietarioSolicitante; }
    public void setPropietarioSolicitante(String propietarioSolicitante) {
        this.propietarioSolicitante = propietarioSolicitante;
    }

    public boolean isDirectorioObjetivo() { return directorioObjetivo; }
    public void setDirectorioObjetivo(boolean directorioObjetivo) {
        this.directorioObjetivo = directorioObjetivo;
    }

    public String getDirectorioPadre() { return directorioPadre; }
    public void setDirectorioPadre(String directorioPadre) {
        this.directorioPadre = (directorioPadre == null || directorioPadre.isBlank()) ? "raiz" : directorioPadre;
    }

    public boolean isEsperandoLock() { return esperandoLock; }
    public void setEsperandoLock(boolean esperandoLock) { this.esperandoLock = esperandoLock; }

    public int getReintentosLock() { return reintentosLock; }
    public void incrementarReintentosLock() { this.reintentosLock++; }
    public void reiniciarReintentosLock() { this.reintentosLock = 0; }

    @Override
    public String toString() {
        String extra = (nombreNuevo != null && !nombreNuevo.isBlank()) ? (" -> " + nombreNuevo) : "";
        String padre = (directorioPadre != null && !directorioPadre.isBlank()) ? (" @" + directorioPadre) : "";
        String espera = esperandoLock ? (" [lock x" + reintentosLock + "]") : "";
        return idProceso + " [" + tipoOperacion + " " + nombreArchivo + extra + padre + "] - " + estado + espera;
    }
}
