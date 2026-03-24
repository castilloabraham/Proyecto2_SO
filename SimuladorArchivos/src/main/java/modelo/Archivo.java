package modelo;

public class Archivo {
    private String nombre;
    private int tamañoEnBloques;
    private int bloqueInicial;
    private String propietario;

    private int lectoresActivos;
    private int lectoresEsperando;
    private int escritoresEsperando;
    private boolean siendoEscrito;

    public Archivo(String nombre, int tamañoEnBloques, int bloqueInicial, String propietario) {
        this.nombre = nombre;
        this.tamañoEnBloques = tamañoEnBloques;
        this.bloqueInicial = bloqueInicial;
        this.propietario = propietario;

        this.lectoresActivos = 0;
        this.lectoresEsperando = 0;
        this.escritoresEsperando = 0;
        this.siendoEscrito = false;
    }

    public String getNombre() { return nombre; }
    public int getTamañoEnBloques() { return tamañoEnBloques; }
    public int getBloqueInicial() { return bloqueInicial; }
    public String getPropietario() { return propietario; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public synchronized boolean intentarLeer() {
        if (siendoEscrito || escritoresEsperando > 0) {
            return false;
        }
        lectoresActivos++;
        return true;
    }

    public synchronized void terminarLeer() {
        if (lectoresActivos > 0) {
            lectoresActivos--;
        }
    }

    public synchronized boolean intentarEscribir() {
        if (siendoEscrito || lectoresActivos > 0) {
            return false;
        }
        siendoEscrito = true;
        return true;
    }

    public synchronized void terminarEscribir() {
        siendoEscrito = false;
    }

    public synchronized void registrarEsperaLectura() {
        lectoresEsperando++;
    }

    public synchronized void cancelarEsperaLectura() {
        if (lectoresEsperando > 0) {
            lectoresEsperando--;
        }
    }

    public synchronized void registrarEsperaEscritura() {
        escritoresEsperando++;
    }

    public synchronized void cancelarEsperaEscritura() {
        if (escritoresEsperando > 0) {
            escritoresEsperando--;
        }
    }

    public synchronized boolean isSiendoEscrito() {
        return siendoEscrito;
    }

    public synchronized int getLectoresActivos() {
        return lectoresActivos;
    }

    public synchronized int getLectoresEsperando() {
        return lectoresEsperando;
    }

    public synchronized int getEscritoresEsperando() {
        return escritoresEsperando;
    }

    public synchronized String getResumenLock() {
        if (siendoEscrito) {
            String espera = escritoresEsperando > 0 || lectoresEsperando > 0
                    ? " | espera L" + lectoresEsperando + "/E" + escritoresEsperando
                    : "";
            return "🔒 Escritura" + espera;
        }

        if (lectoresActivos > 0) {
            String espera = escritoresEsperando > 0 || lectoresEsperando > 0
                    ? " | espera L" + lectoresEsperando + "/E" + escritoresEsperando
                    : "";
            return "👁 Lectura (" + lectoresActivos + ")" + espera;
        }

        if (lectoresEsperando > 0 || escritoresEsperando > 0) {
            return "⏳ Espera L" + lectoresEsperando + "/E" + escritoresEsperando;
        }

        return "Libre";
    }
}
