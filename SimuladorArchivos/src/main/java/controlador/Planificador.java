package controlador;

import estructuras.Cola;
import modelo.Archivo;
import modelo.EntradaJournal;
import modelo.Proceso;

public class Planificador extends Thread {

    private static final int MAX_PROCESOS_CONCURRENTES = 3;

    private final GestorArchivos gestor;
    private volatile boolean ejecutando = true;
    private volatile int velocidadMs = 500;
    private volatile String politicaActual = "FIFO";
    private boolean moviendoDerecha = true;
    private int procesosActivos = 0;

    public Planificador(GestorArchivos gestor) {
        this.gestor = gestor;
    }

    public void setPolitica(String politica) {
        if (politica != null && !politica.isBlank()) {
            this.politicaActual = politica;
        }
    }

    public void setVelocidad(int velocidad) {
        this.velocidadMs = Math.max(50, velocidad);
    }

    public void detenerPlanificador() {
        this.ejecutando = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (ejecutando) {
            try {
                Cola<Proceso> cola = gestor.getColaProcesos();

                if (cola == null || cola.estaVacia()) {
                    Thread.sleep(150);
                    continue;
                }

                if (obtenerProcesosActivos() >= MAX_PROCESOS_CONCURRENTES) {
                    Thread.sleep(120);
                    continue;
                }

                Proceso procesoActual = seleccionarProceso(cola, gestor.getPosicionCabeza());
                if (procesoActual == null) {
                    Thread.sleep(120);
                    continue;
                }

                int posicionActual = gestor.getPosicionCabeza();
                int destino = procesoActual.getBloqueDestino();
                int movimiento = calcularMovimiento(posicionActual, destino);

                procesoActual.setEstado("Despachado");
                gestor.actualizarColaVisual();
                Thread.sleep(velocidadMs);

                if (gestor.getVentana() != null) {
                    gestor.getVentana().actualizarPosicionCabezalEnVivo(destino);
                }
                gestor.setPosicionCabeza(destino);

                lanzarWorker(procesoActual, movimiento);

            } catch (InterruptedException e) {
                if (!ejecutando) {
                    return;
                }
                System.out.println("⚠️ Hilo del planificador interrumpido.");
            } catch (Exception e) {
                System.out.println("❌ ERROR FATAL EN EL PLANIFICADOR:");
                e.printStackTrace();
            }
        }
    }

    private void lanzarWorker(Proceso procesoActual, int movimiento) {
        incrementarProcesosActivos();

        Thread worker = new Thread(() -> {
            Archivo archivoObjetivo = gestor.buscarArchivoObj(procesoActual.getNombreArchivo());
            boolean lockAdquirido = false;
            boolean cambioEnDisco = false;

            try {
                lockAdquirido = esperarYAdquirirLock(procesoActual, archivoObjetivo);
                if (!lockAdquirido && requiereLock(procesoActual, archivoObjetivo)) {
                    procesoActual.setEstado("Cancelado");
                    return;
                }

                procesoActual.setEstado("Ejecutando");
                gestor.refrescarPantallaCompleta();
                Thread.sleep(calcularTiempoServicio(procesoActual));

                cambioEnDisco = ejecutarOperacion(procesoActual, archivoObjetivo);
                procesoActual.setEstado("Terminado");

                String mensaje = "✅ [" + politicaActual + "] Atendió: " + procesoActual.getIdProceso()
                        + " | " + procesoActual.getTipoOperacion() + " " + procesoActual.getNombreArchivo()
                        + ((procesoActual.getNombreNuevo() != null && !procesoActual.getNombreNuevo().isBlank())
                        ? (" -> " + procesoActual.getNombreNuevo()) : "")
                        + " | Viajó " + movimiento + " bloques.";
                gestor.imprimirEnLogVisual(mensaje);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                procesoActual.setEstado("Interrumpido");
            } catch (Exception e) {
                procesoActual.setEstado("Error");
                gestor.imprimirEnLogVisual("❌ Error ejecutando " + procesoActual.getIdProceso() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    liberarLockSiAplica(procesoActual, archivoObjetivo, lockAdquirido);
                } finally {
                    decrementarProcesosActivos();
                    gestor.actualizarColaVisual();
                    if (cambioEnDisco) {
                        gestor.refrescarPantallaCompleta();
                    } else {
                        gestor.refrescarPantallaCompleta();
                    }
                }
            }
        }, "worker-" + procesoActual.getIdProceso());

        worker.setDaemon(true);
        worker.start();
    }

    private synchronized void incrementarProcesosActivos() {
        procesosActivos++;
    }

    private synchronized void decrementarProcesosActivos() {
        if (procesosActivos > 0) {
            procesosActivos--;
        }
    }

    private synchronized int obtenerProcesosActivos() {
        return procesosActivos;
    }

    private Proceso seleccionarProceso(Cola<Proceso> cola, int posicionActual) {
        if (cola.estaVacia()) {
            return null;
        }

        switch (politicaActual) {
            case "FIFO":
                return cola.desencolar();
            case "SSTF":
                return seleccionarSSTF(cola, posicionActual);
            case "SCAN":
                return seleccionarSCAN(cola, posicionActual);
            case "C-SCAN":
                return seleccionarCSCAN(cola, posicionActual);
            default:
                return cola.desencolar();
        }
    }

    private Proceso seleccionarSSTF(Cola<Proceso> cola, int posicionActual) {
        int size = cola.getTamano();
        Proceso mejor = null;
        int minDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            Proceso p = cola.desencolar();
            int distancia = Math.abs(p.getBloqueDestino() - posicionActual);
            if (distancia < minDistancia) {
                minDistancia = distancia;
                mejor = p;
            }
            cola.encolar(p);
        }

        return extraerProcesoSeleccionado(cola, mejor, size);
    }

    private Proceso seleccionarSCAN(Cola<Proceso> cola, int posicionActual) {
        int size = cola.getTamano();
        Proceso mejor = null;
        int minDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            Proceso p = cola.desencolar();
            int destino = p.getBloqueDestino();
            boolean enCamino = (moviendoDerecha && destino >= posicionActual)
                    || (!moviendoDerecha && destino <= posicionActual);

            if (enCamino) {
                int distancia = Math.abs(destino - posicionActual);
                if (distancia < minDistancia) {
                    minDistancia = distancia;
                    mejor = p;
                }
            }
            cola.encolar(p);
        }

        if (mejor == null) {
            moviendoDerecha = !moviendoDerecha;
            minDistancia = Integer.MAX_VALUE;

            for (int i = 0; i < size; i++) {
                Proceso p = cola.desencolar();
                int destino = p.getBloqueDestino();
                boolean enCamino = (moviendoDerecha && destino >= posicionActual)
                        || (!moviendoDerecha && destino <= posicionActual);

                if (enCamino) {
                    int distancia = Math.abs(destino - posicionActual);
                    if (distancia < minDistancia) {
                        minDistancia = distancia;
                        mejor = p;
                    }
                }
                cola.encolar(p);
            }
        }

        return extraerProcesoSeleccionado(cola, mejor, size);
    }

    private Proceso seleccionarCSCAN(Cola<Proceso> cola, int posicionActual) {
        int size = cola.getTamano();
        Proceso mejor = null;
        int minDistancia = Integer.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            Proceso p = cola.desencolar();
            int destino = p.getBloqueDestino();

            if (destino >= posicionActual) {
                int distancia = destino - posicionActual;
                if (distancia < minDistancia) {
                    minDistancia = distancia;
                    mejor = p;
                }
            }
            cola.encolar(p);
        }

        if (mejor == null) {
            int bloqueMasBajo = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                Proceso p = cola.desencolar();
                int destino = p.getBloqueDestino();
                if (destino < bloqueMasBajo) {
                    bloqueMasBajo = destino;
                    mejor = p;
                }
                cola.encolar(p);
            }
        }

        return extraerProcesoSeleccionado(cola, mejor, size);
    }

    private Proceso extraerProcesoSeleccionado(Cola<Proceso> cola, Proceso seleccionado, int size) {
        if (seleccionado == null) {
            return null;
        }

        for (int i = 0; i < size; i++) {
            Proceso p = cola.desencolar();
            if (p != seleccionado) {
                cola.encolar(p);
            }
        }

        return seleccionado;
    }

    private boolean esperarYAdquirirLock(Proceso procesoActual, Archivo archivoObjetivo) throws InterruptedException {
        if (!requiereLock(procesoActual, archivoObjetivo)) {
            return true;
        }

        boolean escritura = esOperacionDeEscritura(procesoActual);
        registrarEsperaSiAplica(procesoActual, archivoObjetivo, escritura);

        while (ejecutando) {
            boolean adquirido = escritura ? archivoObjetivo.intentarEscribir() : archivoObjetivo.intentarLeer();

            if (adquirido) {
                cancelarEsperaSiAplica(procesoActual, archivoObjetivo, escritura);
                procesoActual.setEstado(escritura ? "Ejecutando (lock escritura)" : "Ejecutando (lock lectura)");
                gestor.imprimirEnLogVisual("🔓 [" + procesoActual.getIdProceso() + "] Lock concedido sobre " + procesoActual.getNombreArchivo());
                gestor.refrescarPantallaCompleta();
                return true;
            }

            procesoActual.setEstado("Bloqueado por Lock");
            procesoActual.incrementarReintentosLock();
            if (procesoActual.getReintentosLock() == 1 || procesoActual.getReintentosLock() % 4 == 0) {
                gestor.imprimirEnLogVisual("🔒 [" + procesoActual.getIdProceso() + "] Esperando lock sobre "
                        + procesoActual.getNombreArchivo() + " (intento " + procesoActual.getReintentosLock() + ")");
            }
            gestor.refrescarPantallaCompleta();
            Thread.sleep(250);
        }

        cancelarEsperaSiAplica(procesoActual, archivoObjetivo, escritura);
        return false;
    }

    private void registrarEsperaSiAplica(Proceso procesoActual, Archivo archivoObjetivo, boolean escritura) {
        if (archivoObjetivo == null || procesoActual.isEsperandoLock()) {
            return;
        }

        if (escritura) {
            archivoObjetivo.registrarEsperaEscritura();
        } else {
            archivoObjetivo.registrarEsperaLectura();
        }

        procesoActual.setEsperandoLock(true);
        procesoActual.reiniciarReintentosLock();
        gestor.refrescarPantallaCompleta();
    }

    private void cancelarEsperaSiAplica(Proceso procesoActual, Archivo archivoObjetivo, boolean escritura) {
        if (archivoObjetivo == null || !procesoActual.isEsperandoLock()) {
            return;
        }

        if (escritura) {
            archivoObjetivo.cancelarEsperaEscritura();
        } else {
            archivoObjetivo.cancelarEsperaLectura();
        }

        procesoActual.setEsperandoLock(false);
        procesoActual.reiniciarReintentosLock();
        gestor.refrescarPantallaCompleta();
    }

    private boolean requiereLock(Proceso procesoActual, Archivo archivoObjetivo) {
        if (archivoObjetivo == null || procesoActual == null || procesoActual.isDirectorioObjetivo()) {
            return false;
        }

        String tipo = procesoActual.getTipoOperacion();
        return "LEER".equals(tipo) || "READ".equals(tipo)
                || "ELIMINAR".equals(tipo) || "DELETE".equals(tipo)
                || "ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo);
    }

    private boolean esOperacionDeEscritura(Proceso procesoActual) {
        if (procesoActual == null) {
            return false;
        }

        String tipo = procesoActual.getTipoOperacion();
        return "ELIMINAR".equals(tipo) || "DELETE".equals(tipo)
                || "ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo);
    }

    private void liberarLockSiAplica(Proceso procesoActual, Archivo archivoObjetivo, boolean lockAdquirido) {
        if (!lockAdquirido || archivoObjetivo == null) {
            return;
        }

        String tipo = procesoActual.getTipoOperacion();
        if ("LEER".equals(tipo) || "READ".equals(tipo)) {
            archivoObjetivo.terminarLeer();
        } else if ("ELIMINAR".equals(tipo) || "DELETE".equals(tipo)
                || "ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo)) {
            archivoObjetivo.terminarEscribir();
        }

        gestor.imprimirEnLogVisual("🔓 [" + procesoActual.getIdProceso() + "] Lock liberado de " + procesoActual.getNombreArchivo());
    }

    private int calcularMovimiento(int posicionActual, int destino) {
        int ultimoBloque = gestor.getDisco().getCapacidad() - 1;

        if ("C-SCAN".equals(politicaActual) && destino < posicionActual) {
            return (ultimoBloque - posicionActual) + destino;
        }

        return Math.abs(destino - posicionActual);
    }

    private int calcularTiempoServicio(Proceso procesoActual) {
        String tipo = procesoActual.getTipoOperacion();

        if ("LEER".equals(tipo) || "READ".equals(tipo)) {
            return Math.max(700, velocidadMs);
        }

        return Math.max(900, velocidadMs + 250);
    }

    private boolean ejecutarOperacion(Proceso procesoActual, Archivo archivoObjetivo) {
        String tipo = procesoActual.getTipoOperacion();

        if ("LEER".equals(tipo) || "READ".equals(tipo)) {
            gestor.imprimirEnLogVisual("📖 Leyendo archivo: " + procesoActual.getNombreArchivo());
            return false;
        }

        if ("CREAR".equals(tipo) || "CREATE".equals(tipo)) {
            EntradaJournal entrada = gestor.registrarOperacionPendiente(
                    "CREAR",
                    procesoActual.getNombreArchivo(),
                    procesoActual.getTamano(),
                    -1,
                    procesoActual.getPropietarioSolicitante(),
                    false,
                    procesoActual.getDirectorioPadre()
            );

            boolean exito = gestor.crearArchivo(
                    procesoActual.getNombreArchivo(),
                    procesoActual.getTamano(),
                    procesoActual.getPropietarioSolicitante(),
                    procesoActual.getDirectorioPadre()
            );
            if (!exito) {
                gestor.imprimirEnLogVisual("❌ No se pudo crear '" + procesoActual.getNombreArchivo() + "'.");
                return false;
            }

            Archivo creado = gestor.buscarArchivoObj(procesoActual.getNombreArchivo());
            if (creado != null) {
                entrada.setBloqueInicial(creado.getBloqueInicial());
                procesoActual.setBloqueDestino(creado.getBloqueInicial());
            }

            if (gestor.simularFallo) {
                gestor.imprimirEnLogVisual("💥 ¡SISTEMA CAÍDO! Falla después de crear y antes del commit: CREAR " + procesoActual.getNombreArchivo());
                ejecutando = false;
                return true;
            }

            gestor.confirmarOperacionJournal(entrada);
            return true;
        }

        if ("ELIMINAR".equals(tipo) || "DELETE".equals(tipo)) {
            EntradaJournal entrada = gestor.construirSnapshotParaEliminacion(procesoActual.getNombreArchivo());
            if (entrada == null) {
                gestor.imprimirEnLogVisual("❌ No se pudo preparar journal para eliminar '" + procesoActual.getNombreArchivo() + "'.");
                return false;
            }
            gestor.journal.agregar(entrada);
            gestor.imprimirEnLogVisual("📝 Journal: PENDIENTE ELIMINAR " + procesoActual.getNombreArchivo());

            boolean exito = gestor.eliminarArchivo(procesoActual.getNombreArchivo());
            if (!exito) {
                exito = gestor.eliminarDirectorio(procesoActual.getNombreArchivo());
            }

            if (!exito) {
                gestor.imprimirEnLogVisual("❌ No se pudo eliminar '" + procesoActual.getNombreArchivo() + "'.");
                return false;
            }

            gestor.confirmarOperacionJournal(entrada);
            return true;
        }

        if ("ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo)) {
            String nombreNuevo = (procesoActual.getNombreNuevo() != null && !procesoActual.getNombreNuevo().isBlank())
                    ? procesoActual.getNombreNuevo()
                    : generarNombreActualizado(procesoActual.getNombreArchivo());

            boolean exito = gestor.renombrarItem(procesoActual.getNombreArchivo(), nombreNuevo);

            if (exito) {
                gestor.imprimirEnLogVisual("✏️ UPDATE aplicado: " + procesoActual.getNombreArchivo() + " -> " + nombreNuevo);
                return true;
            }

            gestor.imprimirEnLogVisual("❌ No se pudo actualizar '" + procesoActual.getNombreArchivo() + "'.");
            return false;
        }

        gestor.imprimirEnLogVisual("⚠️ Operación no reconocida: " + tipo);
        return false;
    }

    private String generarNombreActualizado(String nombreBase) {
        int punto = nombreBase.lastIndexOf('.');
        if (punto > 0) {
            String base = nombreBase.substring(0, punto);
            String ext = nombreBase.substring(punto);
            return base + "_upd" + ext;
        }
        return nombreBase + "_upd";
    }
}
