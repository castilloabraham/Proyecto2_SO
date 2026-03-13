package estructuras;

public class Nodo<T> {
    private T data;
    private Nodo<T> siguiente;

    // Constructor
    public Nodo(T data) {
        this.data = data;
        this.siguiente = null;
    }

    // Getters y Setters
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Nodo<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo<T> siguiente) {
        this.siguiente = siguiente;
    }
}