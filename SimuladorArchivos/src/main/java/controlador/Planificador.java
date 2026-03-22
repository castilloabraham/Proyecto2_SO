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
                        
                    } else if (politicaActual.equals("SCAN")) {
                        // --- INICIO ALGORITMO SCAN (Ascensor) ---
                        int size = cola.getTamano();
                        int minDistancia = Integer.MAX_VALUE;
                        
                        // 1era Vuelta: Buscar el más cercano EN LA DIRECCIÓN ACTUAL
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            int destino = p.getBloqueDestino();
                            
                            // ¿Está en nuestro camino?
                            boolean enCamino = (moviendoDerecha && destino >= posicionActual) || 
                                               (!moviendoDerecha && destino <= posicionActual);
                            
                            if (enCamino) {
                                int distancia = Math.abs(destino - posicionActual);
                                if (distancia < minDistancia) {
                                    minDistancia = distancia;
                                    procesoActual = p; // Tenemos un candidato en nuestra dirección
                                }
                            }
                            cola.encolar(p); // Regresa a la fila temporalmente
                        }
                        
                        // 2da Vuelta (Solo si es necesario): Si no encontramos a nadie en nuestro camino, 
                        // el ascensor CAMBIA DE DIRECCIÓN y busca de nuevo.
                        if (procesoActual == null) {
                            moviendoDerecha = !moviendoDerecha; // ¡Metemos reversa!
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

                        // 3era Vuelta: Ya tenemos al ganador definitivo, lo sacamos de la cola
                        for (int i = 0; i < size; i++) {
                            Proceso p = cola.desencolar();
                            if (p != procesoActual) {
                                cola.encolar(p); // Los demás siguen esperando
                            }
                        }
                        // --- FIN ALGORITMO SCAN ---
                    } else {
                        // Por si acaso eligen alguna otra (C-SCAN) o hay error, usamos FIFO por defecto
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