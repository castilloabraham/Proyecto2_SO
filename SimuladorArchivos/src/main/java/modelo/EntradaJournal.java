package modelo;

public class EntradaJournal {
    private String tipoOperacion;
    private String nombreArchivo;
    private int tamano;
    private int bloqueInicial;
    private String propietario;
    private boolean directorio;
    private String estado; // PENDIENTE, CONFIRMADA, UNDO
    private String directorioPadre;

    public EntradaJournal(String tipoOperacion, String nombreArchivo, int tamano,
                          int bloqueInicial, String propietario, boolean directorio, String estado) {
        this(tipoOperacion, nombreArchivo, tamano, bloqueInicial, propietario, directorio, estado, "raiz");
    }

    public EntradaJournal(String tipoOperacion, String nombreArchivo, int tamano,
                          int bloqueInicial, String propietario, boolean directorio,
                          String estado, String directorioPadre) {
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.tamano = tamano;
        this.bloqueInicial = bloqueInicial;
        this.propietario = propietario;
        this.directorio = directorio;
        this.estado = estado;
        this.directorioPadre = (directorioPadre == null || directorioPadre.isBlank()) ? "raiz" : directorioPadre;
    }

    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getTamano() { return tamano; }
    public int getBloqueInicial() { return bloqueInicial; }
    public void setBloqueInicial(int bloqueInicial) { this.bloqueInicial = bloqueInicial; }
    public String getPropietario() { return propietario; }
    public boolean isDirectorio() { return directorio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDirectorioPadre() { return directorioPadre; }
    public void setDirectorioPadre(String directorioPadre) {
        this.directorioPadre = (directorioPadre == null || directorioPadre.isBlank()) ? "raiz" : directorioPadre;
    }
}
