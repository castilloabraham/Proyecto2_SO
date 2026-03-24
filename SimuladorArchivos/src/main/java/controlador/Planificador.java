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
                        procesoActual = cola.desencolar(); // Por si acaso (Default a FIFO)
                    }
                    // --- FIN DE LA LÓGICA DE POLÍTICAS ---


                    modelo.Archivo archivoObjetivo = gestor.buscarArchivoObj(procesoActual.getNombreArchivo());
                    boolean puedeEjecutar = true;

                    // NOTA: Si es una creación, el archivo aún no existe, así que archivoObjetivo será null y puede ejecutar libremente.
                    if (archivoObjetivo != null) {
                        if (procesoActual.getTipoOperacion().equals("LEER")) {
                            puedeEjecutar = archivoObjetivo.intentarLeer();
                        } else {
                            // ACTUALIZAR o ELIMINAR exigen lock exclusivo
                            puedeEjecutar = archivoObjetivo.intentarEscribir();
                        }
                    }

                    if (!puedeEjecutar) {
                        // El archivo está bloqueado, devolvemos el proceso a la cola
                        procesoActual.setEstado("Bloqueado por Lock");
                        cola.encolar(procesoActual);
                        gestor.imprimirEnLogVisual("🔒 [" + procesoActual.getIdProceso() + "] Bloqueado: Esperando acceso a " + procesoActual.getNombreArchivo());
                        gestor.actualizarColaVisual();
                        Thread.sleep(500); // Pausa breve antes de intentar con el siguiente proceso
                        continue; // SALTA de vuelta al inicio del while (No mueve el disco)
                    }
                    // =========================================================


                    // Si pasó la verificación de los Locks, el proceso se ejecuta
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
                    Thread.sleep(velocidadMs); // El disco viaja... (Pausa que simula el hardware)
                    
                    // Le avisa a la ventana que cambie el texto del cabezal
                    if (gestor.getVentana() != null) {
                        gestor.getVentana().actualizarPosicionCabezalEnVivo(destino);
                    }
                    
                    gestor.setPosicionCabeza(destino);
                    
                    boolean cambioEnDisco = false;
                    
                    if (procesoActual.getTipoOperacion().equals("CREAR")) {
                        // 1. Registrar en Journal como PENDIENTE
                        String transaccion = "CREAR " + procesoActual.getNombreArchivo();
                        gestor.journal.agregar("PENDIENTE: " + transaccion);
                        gestor.imprimirEnLogVisual("📝 Journal: PENDIENTE " + transaccion);

                        // 2. Punto crítico de fallo
                        if (gestor.simularFallo) {
                            gestor.imprimirEnLogVisual("💥 ¡SISTEMA CAÍDO! Falla durante: " + transaccion);
                            break; // Rompe el hilo simulando un Crash
                        }

                        // 3. Ejecutar y Confirmar
                        boolean exito = gestor.crearArchivo(procesoActual.getNombreArchivo(), procesoActual.getTamano(), "admin");
                        if (exito) {
                            gestor.journal.agregar("CONFIRMADA: " + transaccion);
                            gestor.imprimirEnLogVisual("✅ Journal: CONFIRMADA " + transaccion);
                            cambioEnDisco = true;
                        }

                    } else if (procesoActual.getTipoOperacion().equals("ELIMINAR")) {
                        String transaccion = "ELIMINAR " + procesoActual.getNombreArchivo();
                        gestor.journal.agregar("PENDIENTE: " + transaccion);
                        gestor.imprimirEnLogVisual("📝 Journal: PENDIENTE " + transaccion);

                        if (gestor.simularFallo) {
                            gestor.imprimirEnLogVisual("💥 ¡SISTEMA CAÍDO! Falla durante: " + transaccion);
                            break;
                        }

                        boolean exito = gestor.eliminarArchivo(procesoActual.getNombreArchivo());
                        if (!exito) exito = gestor.eliminarDirectorio(procesoActual.getNombreArchivo());
                        
                        if (exito) {
                            gestor.journal.agregar("CONFIRMADA: " + transaccion);
                            gestor.imprimirEnLogVisual("✅ Journal: CONFIRMADA " + transaccion);
                            cambioEnDisco = true;
                        }
                    }
                    
                    procesoActual.setEstado("Terminado");
                    // =========================================================


                    // --- LIBERAR LOS LOCKS AL TERMINAR ---
                    if (archivoObjetivo != null) {
                        if (procesoActual.getTipoOperacion().equals("LEER")) {
                            archivoObjetivo.terminarLeer();
                        } else {
                            archivoObjetivo.terminarEscribir();
                        }
                    }
                    // =========================================================

                    String mensaje = "✅ [" + politicaActual + "] Atendió: " + procesoActual.getIdProceso() + 
                                     " | " + procesoActual.getTipoOperacion() + " " + procesoActual.getNombreArchivo() +
                                     " | Viajó " + movimiento + " bloques.";
                    
                    gestor.imprimirEnLogVisual(mensaje); 
                    gestor.actualizarColaVisual();
                    
                    // Si se creó o borró algo, le pedimos al gestor que redibuje la pantalla
                    if (cambioEnDisco) {
                        gestor.refrescarPantallaCompleta();
                    }
                    
                } else {
                    Thread.sleep(200); // Descansa si no hay trabajo en la cola
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