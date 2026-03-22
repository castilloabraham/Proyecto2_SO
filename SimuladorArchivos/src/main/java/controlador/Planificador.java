package controlador;

import modelo.Proceso;
import estructuras.Cola;

public class Planificador extends Thread {
    
    private GestorArchivos gestor;
    private boolean ejecutando = true;
    private int velocidadMs = 500; // Tiempo que tarda en moverse (Luego lo conectaremos a tu Slider)
    private String politicaActual = "FIFO"; // Por defecto iniciamos en FIFO

    public Planificador(GestorArchivos gestor) {
        this.gestor = gestor;
    }

    public void setPolitica(String politica) {
        this.politicaActual = politica;
    }
    
    public void setVelocidad(int velocidad) {
        // Mapeamos el valor del slider (0-100) a milisegundos reales (ej. 1000ms a 50ms)
        this.velocidadMs = 1000 - (velocidad * 9); 
    }

    @Override
    public void run() {
        while (ejecutando) {
            Cola<Proceso> cola = gestor.getColaProcesos();
            
            if (!cola.estaVacia()) {
                // 1. Elegimos el proceso (Por ahora programaremos FIFO, el más fácil)
                Proceso procesoActual = cola.desencolar(); 
                procesoActual.setEstado("Ejecutando");
                
                int destino = procesoActual.getBloqueDestino();
                int posicionActual = gestor.getPosicionCabeza();
                
                // Calculamos cuánto viaja la cabeza
                int movimiento = Math.abs(destino - posicionActual);
                
                try {
                    // Actualizamos la pantalla para mostrar que hay un proceso en ejecución
                    gestor.actualizarColaVisual();

                    // 2. Simulamos el viaje del disco
                    Thread.sleep(velocidadMs); 
                    
                    // 3. ¡Llegó al bloque! Actualizamos el sistema
                    gestor.setPosicionCabeza(destino);
                    procesoActual.setEstado("Terminado");
                    
                    // 4. IMPRIMIMOS EN LA PANTALLA DE TU INTERFAZ (Reemplaza el System.out.println)
                    gestor.imprimirEnLogVisual("✅ [Scheduler] Atendió: " + procesoActual.getIdProceso() + 
                                               " | Viajó " + movimiento + " bloques hasta el bloque " + destino);
                    
                    // 5. Refrescamos la cola visual para mostrar los cambios
                    gestor.actualizarColaVisual();
                    
                } catch (InterruptedException e) {
                    procesoActual.setEstado("Bloqueado");
                }
            } else {
                // Si la cola está vacía, el planificador descansa un poco para no saturar la PC
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}