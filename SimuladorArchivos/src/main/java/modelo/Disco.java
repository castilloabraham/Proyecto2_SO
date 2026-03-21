package modelo;

public class Disco {
    private Bloque[] bloques;
    private int capacidad;

    public Disco(int capacidad) {
        this.capacidad = capacidad;
        this.bloques = new Bloque[capacidad];
        
        // Cuando el disco nace, creamos todos los bloques y los marcamos como libres
        for (int i = 0; i < capacidad; i++) {
            this.bloques[i] = new Bloque(i);
        }
    }

    // Método para buscar cuántos bloques libres quedan en el disco
    public int obtenerEspacioLibre() {
        int libres = 0;
        for (int i = 0; i < capacidad; i++) {
            if (bloques[i].isLibre()) {
                libres++;
            }
        }
        return libres;
    }

    public Bloque[] getBloques() {
        return bloques;
    }

    public int getCapacidad() {
        return capacidad;
    }
}