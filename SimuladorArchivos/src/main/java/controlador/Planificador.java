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
        // Ahora el slider nos manda los milisegundos directos (100 a 2000)
        this.velocidadMs = velocidad; 
    }

    @Override
    public void run() {
        while (ejecutando) {
            try {
                Cola<Proceso> cola = gestor.getColaProcesos();
                
                if (!cola.estaVacia()) {
                    Proceso procesoActual = cola.desencolar(); 
                    procesoActual.setEstado("Ejecutando");
                    
                    int destino = procesoActual.getBloqueDestino();
                    int posicionActual = gestor.getPosicionCabeza();
                    int movimiento = Math.abs(destino - posicionActual);
                    
                    // Aviso en consola para saber que el hilo sigue vivo
                    System.out.println("-> [Debug] Intentando atender proceso: " + procesoActual.getIdProceso());
                    
                    gestor.actualizarColaVisual();
                    Thread.sleep(velocidadMs); 
                    
                    gestor.setPosicionCabeza(destino);
                    procesoActual.setEstado("Terminado");
                    
                    String mensaje = "✅ [Scheduler] Atendió: " + procesoActual.getIdProceso() + 
                                     " | Viajó " + movimiento + " bloques hasta el bloque " + destino;
                    
                    // Imprimimos en ambas partes por si la interfaz falla
                    System.out.println(mensaje); 
                    gestor.imprimirEnLogVisual(mensaje); 
                    
                    gestor.actualizarColaVisual();
                    
                } else {
                    // Descanso cuando está vacía
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                System.out.println("⚠️ Hilo interrumpido.");
            } catch (Exception e) {
                // ¡AQUÍ ESTÁ LA MAGIA! Si la interfaz lo rompe, lo veremos aquí.
                System.out.println("❌ ERROR FATAL EN EL PLANIFICADOR:");
                e.printStackTrace();
            }
        }
    }
}