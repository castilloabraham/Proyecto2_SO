package modelo;

public class Bloque {
    private int id;
    private boolean libre;
    private String contenido; // Lo que guarda el bloque
    private String archivoAsignado;
    private int siguienteBloque;

    public Bloque(int id) {
        this.id = id;
        this.libre = true; // Todo bloque nace libre
        this.contenido = "";
        this.archivoAsignado = "Ninguno";
        this.siguienteBloque = -1;
    }
    public int getSiguienteBloque() { return siguienteBloque; }
    public void setSiguienteBloque(int siguienteBloque) { this.siguienteBloque = siguienteBloque; }
    public int getId() { return id; }
    
    public boolean isLibre() { return libre; }
    
    public void setLibre(boolean libre) { this.libre = libre; }
    
    public String getContenido() { return contenido; }
    
    public void setContenido(String contenido) { this.contenido = contenido; }
    
    public String getArchivoAsignado() { return archivoAsignado; }
    public void setArchivoAsignado(String archivoAsignado) { this.archivoAsignado = archivoAsignado; }
}