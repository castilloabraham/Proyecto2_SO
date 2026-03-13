package estructuras;

public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }

    // Agrega un elemento al final de la cola
    public void encolar(T data) {
        Nodo<T> nuevo = new Nodo<>(data);
        if (estaVacia()) {
            frente = nuevo;
            finalCola = nuevo;
        } else {
            finalCola.setSiguiente(nuevo);
            finalCola = nuevo;
        }
        tamano++;
    }

    // Saca y devuelve el primer elemento de la cola
    public T desencolar() {
        if (estaVacia()) return null;
        T data = frente.getData();
        frente = frente.getSiguiente();
        if (frente == null) {
            finalCola = null;
        }
        tamano--;
        return data;
    }

    // Mira el primer elemento sin sacarlo
    public T verFrente() {
        if (estaVacia()) return null;
        return frente.getData();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamano() {
        return tamano;
    }
}