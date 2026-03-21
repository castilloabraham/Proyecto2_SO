package estructuras;

public class ListaEnlazada<T> {
    private Nodo<T> cabeza;
    private int tamano;

    public ListaEnlazada() {
        this.cabeza = null;
        this.tamano = 0;
    }

    // Agrega un elemento al final de la lista
    public void agregar(T data) {
        Nodo<T> nuevo = new Nodo<>(data);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamano++;
    }

    // Obtiene un elemento en una posición específica
    public T obtener(int index) {
        if (index < 0 || index >= tamano) return null;
        Nodo<T> actual = cabeza;
        for (int i = 0; i < index; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getData();
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamano() {
        return tamano;
    }
    
    public void eliminar(int index) {
        if (index < 0 || index >= tamano) return; // Si el índice no existe, no hace nada

        if (index == 0) {
            cabeza = cabeza.getSiguiente(); // Si es el primero, la cabeza salta al segundo
        } else {
            Nodo<T> actual = cabeza;
            // Buscamos el nodo ANTERIOR al que queremos borrar
            for (int i = 0; i < index - 1; i++) {
                actual = actual.getSiguiente();
            }
            // Saltamos el nodo que queremos borrar
            actual.setSiguiente(actual.getSiguiente().getSiguiente());
        }
        tamano--;
    }
}