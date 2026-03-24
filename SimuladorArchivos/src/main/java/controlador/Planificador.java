package controlador;

import estructuras.Cola;
import modelo.Archivo;
import modelo.EntradaJournal;
import modelo.Proceso;

public class Planificador extends Thread {

    private final GestorArchivos gestor;
    private boolean ejecutando = true;
    private int velocidadMs = 500;
    private String politicaActual = "FIFO";
    private boolean moviendoDerecha = true;

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
                    Thread.sleep(200);
                    continue;
                }

                Proceso procesoActual = seleccionarProceso(cola, gestor.getPosicionCabeza());
                if (procesoActual == null) {
                    Thread.sleep(150);
                    continue;
                }

                Archivo archivoObjetivo = gestor.buscarArchivoObj(procesoActual.getNombreArchivo());
                boolean puedeEjecutar = adquirirLockSiAplica(procesoActual, archivoObjetivo);

                if (!puedeEjecutar) {
                    procesoActual.setEstado("Bloqueado por Lock");
                    cola.encolar(procesoActual);
                    gestor.imprimirEnLogVisual("🔒 [" + procesoActual.getIdProceso() + "] Bloqueado: Esperando acceso a " + procesoActual.getNombreArchivo());
                    gestor.actualizarColaVisual();
                    Thread.sleep(300);
                    continue;
                }

                procesoActual.setEstado("Ejecutando");
                int posicionActual = gestor.getPosicionCabeza();
                int destino = procesoActual.getBloqueDestino();
                int movimiento = calcularMovimiento(posicionActual, destino);

                gestor.actualizarColaVisual();
                Thread.sleep(velocidadMs);

                if (gestor.getVentana() != null) {
                    gestor.getVentana().actualizarPosicionCabezalEnVivo(destino);
                }

                gestor.setPosicionCabeza(destino);

                boolean cambioEnDisco = ejecutarOperacion(procesoActual, archivoObjetivo);
                procesoActual.setEstado("Terminado");

                liberarLockSiAplica(procesoActual, archivoObjetivo);

                String mensaje = "✅ [" + politicaActual + "] Atendió: " + procesoActual.getIdProceso()
                        + " | " + procesoActual.getTipoOperacion() + " " + procesoActual.getNombreArchivo()
                        + ((procesoActual.getNombreNuevo() != null && !procesoActual.getNombreNuevo().isBlank()) ? (" -> " + procesoActual.getNombreNuevo()) : "")
                        + " | Viajó " + movimiento + " bloques.";

                gestor.imprimirEnLogVisual(mensaje);
                gestor.actualizarColaVisual();

                if (cambioEnDisco) {
                    gestor.refrescarPantallaCompleta();
                }

            } catch (InterruptedException e) {
                if (!ejecutando) {
                    return;
                }
                System.out.println("⚠️ Hilo interrumpido.");
            } catch (Exception e) {
                System.out.println("❌ ERROR FATAL EN EL PLANIFICADOR:");
                e.printStackTrace();
            }
        }
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

    private boolean adquirirLockSiAplica(Proceso procesoActual, Archivo archivoObjetivo) {
        if (archivoObjetivo == null) {
            return true;
        }

        String tipo = procesoActual.getTipoOperacion();
        if ("LEER".equals(tipo) || "READ".equals(tipo)) {
            return archivoObjetivo.intentarLeer();
        }

        if ("ELIMINAR".equals(tipo) || "DELETE".equals(tipo)
                || "ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo)) {
            return archivoObjetivo.intentarEscribir();
        }

        return true;
    }

    private void liberarLockSiAplica(Proceso procesoActual, Archivo archivoObjetivo) {
        if (archivoObjetivo == null) {
            return;
        }

        String tipo = procesoActual.getTipoOperacion();
        if ("LEER".equals(tipo) || "READ".equals(tipo)) {
            archivoObjetivo.terminarLeer();
        } else if ("ELIMINAR".equals(tipo) || "DELETE".equals(tipo)
                || "ACTUALIZAR".equals(tipo) || "UPDATE".equals(tipo)) {
            archivoObjetivo.terminarEscribir();
        }
    }

    private int calcularMovimiento(int posicionActual, int destino) {
        int ultimoBloque = gestor.getDisco().getCapacidad() - 1;

        if ("C-SCAN".equals(politicaActual) && destino < posicionActual) {
            return (ultimoBloque - posicionActual) + destino;
        }

        return Math.abs(destino - posicionActual);
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
