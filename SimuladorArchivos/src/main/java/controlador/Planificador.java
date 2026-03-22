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
                    
                    Proceso procesoActual = null;
                    int posicionActual = gestor.getPosicionCabeza();

                    // --- INICIO DE LA LÓGICA DE POLÍTICAS ---
                    if (politicaActual.equals("FIFO")) {
                        // FIFO: Saca el primero de la fila (El clásico)
                        procesoActual = cola.desencolar(); 
                        
                    } else if (politicaActual.equals("SSTF")) {
                        // SSTF: Busca el más cercano
                        int size = cola.getTamano();
                        int minDistancia = Integer.MAX_VALUE;

                        // 1era Vuelta: Revisamos todos para ver cuál es el más cercano
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            int distancia = Math.abs(p.getBloqueDestino() - posicionActual);
                            
                            if (distancia < minDistancia) {
                                minDistancia = distancia;
                                procesoActual = p; // Guardamos al ganador
                            }
                            cola.encolar(p); // Lo regresamos a la cola temporalmente
                        }

                        // 2da Vuelta: Sacamos al ganador definitivo de la cola
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            if (p != procesoActual) {
                                cola.encolar(p); // Si no es el ganador, sigue haciendo fila
                            }
                        }
                    } else {
                        // Si eligen SCAN o C-SCAN (que aún no programamos), usamos FIFO por defecto
                        procesoActual = cola.desencolar();
                    }
                    // --- FIN DE LA LÓGICA DE POLÍTICAS ---

                    procesoActual.setEstado("Ejecutando");
                    int destino = procesoActual.getBloqueDestino();
                    int movimiento = Math.abs(destino - posicionActual);
                    
                    gestor.actualizarColaVisual();
                    Thread.sleep(velocidadMs); // El disco viaja...
                    
                    gestor.setPosicionCabeza(destino);
                    procesoActual.setEstado("Terminado");
                    
                    String mensaje = "✅ [" + politicaActual + "] Atendió: " + procesoActual.getIdProceso() + 
                                     " | Viajó " + movimiento + " bloques hasta el bloque " + destino;
                    
                    gestor.imprimirEnLogVisual(mensaje); 
                    gestor.actualizarColaVisual();
                    
                } else {
                    Thread.sleep(200); // Descansa si no hay trabajo
                }
            } catch (InterruptedException e) {
                System.out.println("⚠️ Hilo interrumpido.");
            } catch (Exception e) {
                System.out.println("❌ ERROR FATAL EN EL PLANIFICADOR:");
                e.printStackTrace();
            }
        }
    }
}