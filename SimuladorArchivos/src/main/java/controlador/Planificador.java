package controlador;

import modelo.Proceso;
import estructuras.Cola;

public class Planificador extends Thread {
    
    private GestorArchivos gestor;
    private boolean ejecutando = true;
    private int velocidadMs = 500; // Tiempo que tarda en moverse (Luego lo conectaremos a tu Slider)
    private String politicaActual = "FIFO"; // Por defecto iniciamos en FIFO
    private boolean moviendoDerecha = true; // true = hacia el 99, false = hacia el 0

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
                        // FIFO: Saca el primero de la fila
                        procesoActual = cola.desencolar(); 
                        
                    } else if (politicaActual.equals("SSTF")) {
                        // SSTF: Busca el más cercano
                        int size = cola.getTamano();
                        int minDistancia = Integer.MAX_VALUE;

                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            int distancia = Math.abs(p.getBloqueDestino() - posicionActual);
                            
                            if (distancia < minDistancia) {
                                minDistancia = distancia;
                                procesoActual = p;
                            }
                            cola.encolar(p); 
                        }

                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            if (p != procesoActual) {
                                cola.encolar(p); 
                            }
                        }
                        
                    } else if (politicaActual.equals("SCAN")) {
                        // SCAN: Ascensor con reversa
                        int size = cola.getTamano();
                        int minDistancia = Integer.MAX_VALUE;
                        
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            int destino = p.getBloqueDestino();
                            
                            boolean enCamino = (moviendoDerecha && destino >= posicionActual) || 
                                               (!moviendoDerecha && destino <= posicionActual);
                            
                            if (enCamino) {
                                int distancia = Math.abs(destino - posicionActual);
                                if (distancia < minDistancia) {
                                    minDistancia = distancia;
                                    procesoActual = p; 
                                }
                            }
                            cola.encolar(p); 
                        }
                        
                        if (procesoActual == null) {
                            moviendoDerecha = !moviendoDerecha; // Reversa
                            minDistancia = Integer.MAX_VALUE;
                            
                            for (int i = 0; i < size; i++) {
                                Proceso p = cola.desencolar();
                                int destino = p.getBloqueDestino();
                                
                                boolean enCamino = (moviendoDerecha && destino >= posicionActual) || 
                                                   (!moviendoDerecha && destino <= posicionActual);
                                
                                if (enCamino) {
                                    int distancia = Math.abs(destino - posicionActual);
                                    if (distancia < minDistancia) {
                                        minDistancia = distancia;
                                        procesoActual = p;
                                    }
                                }
                                cola.encolar(p);
                            }
                        }

                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            if (p != procesoActual) {
                                cola.encolar(p); 
                            }
                        }

                    } else if (politicaActual.equals("C-SCAN")) {
                        // --- INICIO ALGORITMO C-SCAN (Ascensor Circular) ---
                        int size = cola.getTamano();
                        int minDistancia = Integer.MAX_VALUE;
                        
                        // 1era Vuelta: Buscar el más cercano siempre hacia la DERECHA (subiendo)
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            int destino = p.getBloqueDestino();
                            
                            if (destino >= posicionActual) { // Solo miramos hacia adelante
                                int distancia = destino - posicionActual;
                                if (distancia < minDistancia) {
                                    minDistancia = distancia;
                                    procesoActual = p;
                                }
                            }
                            cola.encolar(p);
                        }
                        
                        // 2da Vuelta: Si no hay nadie adelante, el ascensor SALTA AL INICIO (0)
                        // Buscamos el bloque que esté más cerca del 0 (el más bajo de todos)
                        if (procesoActual == null) {
                            int bloqueMasBajo = Integer.MAX_VALUE;
                            
                            for (int i = 0; i < size; i++) {
                                Proceso p = cola.desencolar();
                                int destino = p.getBloqueDestino();
                                
                                if (destino < bloqueMasBajo) {
                                    bloqueMasBajo = destino;
                                    procesoActual = p;
                                }
                                cola.encolar(p);
                            }
                        }

                        // 3era Vuelta: Sacar al ganador definitivo de la cola
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            if (p != procesoActual) {
                                cola.encolar(p);
                            }
                        }
                        // --- FIN ALGORITMO C-SCAN ---

                    } else {
                        procesoActual = cola.desencolar(); // Por si acaso
                    }
                    // --- FIN DE LA LÓGICA DE POLÍTICAS ---

                    procesoActual.setEstado("Ejecutando");
                    int destino = procesoActual.getBloqueDestino();
                    
                    // Calculamos la distancia total. Si es C-SCAN y regresó al inicio, 
                    // el movimiento real suma el salto.
                    int movimiento;
                    if (politicaActual.equals("C-SCAN") && destino < posicionActual) {
                        movimiento = (99 - posicionActual) + destino; // Salto al final y regreso al inicio
                    } else {
                        movimiento = Math.abs(destino - posicionActual);
                    }
                    
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