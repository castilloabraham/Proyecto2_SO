package modelo;

public class Archivo {
    private String nombre;
    private int tamañoEnBloques;
    private int bloqueInicial;
    private String propietario; // Para la tabla

    public Archivo(String nombre, int tamañoEnBloques, int bloqueInicial, String propietario) {
        this.nombre = nombre;
        this.tamañoEnBloques = tamañoEnBloques;
        this.bloqueInicial = bloqueInicial;
        this.propietario = propietario;
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getTamañoEnBloques() { return tamañoEnBloques; }
    public int getBloqueInicial() { return bloqueInicial; }
    public String getPropietario() { return propietario; }
}