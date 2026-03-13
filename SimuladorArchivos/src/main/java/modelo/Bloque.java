package modelo;

public class Bloque {
    private int id;
    private boolean libre;
    private String contenido; // Lo que guarda el bloque

    public Bloque(int id) {
        this.id = id;
        this.libre = true; // Todo bloque nace libre
        this.contenido = "";
    }

    public int getId() { return id; }
    
    public boolean isLibre() { return libre; }
    
    public void setLibre(boolean libre) { this.libre = libre; }
    
    public String getContenido() { return contenido; }
    
    public void setContenido(String contenido) { this.contenido = contenido; }
}