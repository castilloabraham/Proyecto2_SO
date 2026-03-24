package controlador;

import modelo.Archivo;
import modelo.Bloque;
import modelo.Directorio;
import modelo.Disco;
import modelo.Proceso;
import estructuras.Cola;
import estructuras.ListaEnlazada;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;

public class GestorArchivos {

    private Disco disco;
    private Directorio directorioRaiz;

    private Cola<Proceso> colaProcesos;
    private int contadorProcesos = 1;
    private Planificador planificador;

    private interfaz.VentanaPrincipal ventana;

    // Se mantiene como String para que compile con tu Planificador actual
    public ListaEnlazada<String> journal = new ListaEnlazada<>();
    public boolean simularFallo = false;

    private int posicionCabeza = 0;

    public GestorArchivos() {
        this.disco = new Disco(100);
        this.directorioRaiz = new Directorio("raiz");
        this.colaProcesos = new Cola<>();
        this.planificador = new Planificador(this);
        this.planificador.start();
    }

    public void setVentana(interfaz.VentanaPrincipal ventana) {
        this.ventana = ventana;
    }

    public interfaz.VentanaPrincipal getVentana() {
        return this.ventana;
    }

    public Cola<Proceso> getColaProcesos() {
        return colaProcesos;
    }

    public Disco getDisco() {
        return disco;
    }

    public Directorio getDirectorioRaiz() {
        return directorioRaiz;
    }

    public int getPosicionCabeza() {
        return posicionCabeza;
    }

    public void setPosicionCabeza(int posicion) {
        this.posicionCabeza = posicion;
    }

    public void imprimirEnLogVisual(String mensaje) {
        if (this.ventana != null) {
            this.ventana.agregarMensajeLog(mensaje);
        }
    }

    public void actualizarColaVisual() {
        if (this.ventana == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== COLA DE PROCESOS (I/O) ===\n");
        sb.append("Total esperando: ").append(colaProcesos.getTamano()).append("\n\n");

        int size = colaProcesos.getTamano();
        for (int i = 0; i < size; i++) {
            Proceso p = colaProcesos.desencolar();

            sb.append(" ⚙️ [").append(p.getIdProceso()).append("] ");
            sb.append(p.getTipoOperacion()).append(" '").append(p.getNombreArchivo()).append("' ");
            sb.append("-> Destino: Blk ").append(p.getBloqueDestino());

            if (p.getEstado() != null && p.getEstado().contains("Bloqueado")) {
                sb.append(" (🔒 BLOQUEADO)\n");
            } else {
                sb.append(" (⏳ ").append(p.getEstado()).append(")\n");
            }

            colaProcesos.encolar(p);
        }

        this.ventana.actualizarPantallaProcesos(sb.toString());
    }

    public void refrescarPantallaCompleta() {
        if (this.ventana != null) {
            this.ventana.actualizarPantallaCompleta();
        }
    }

    public void cambiarVelocidadDisco(int nuevaVelocidad) {
        if (this.planificador != null) {
            this.planificador.setVelocidad(nuevaVelocidad);
        }
    }

    public void cambiarPoliticaPlanificador(String nuevaPolitica) {
        if (this.planificador != null) {
            this.planificador.setPolitica(nuevaPolitica);
        }
    }

    private boolean existeNombreGlobal(String nombre) {
        return buscarArchivoObj(nombre) != null || buscarDirectorioObj(nombre) != null;
    }

    public Archivo buscarArchivoObj(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return buscarArchivoRecursivo(directorioRaiz, nombre.trim());
    }

    private Archivo buscarArchivoRecursivo(Directorio actual, String nombre) {
        ListaEnlazada<Archivo> archivos = actual.getArchivos();
        for (int i = 0; i < archivos.getTamano(); i++) {
            Archivo arch = archivos.obtener(i);
            if (arch.getNombre().equals(nombre)) {
                return arch;
            }
        }

        ListaEnlazada<Directorio> subdirs = actual.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            Archivo encontrado = buscarArchivoRecursivo(subdirs.obtener(i), nombre);
            if (encontrado != null) {
                return encontrado;
            }
        }

        return null;
    }

    public Directorio buscarDirectorioObj(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        if (directorioRaiz.getNombre().equals(nombre.trim())) {
            return directorioRaiz;
        }

        return buscarDirectorioRecursivo(directorioRaiz, nombre.trim());
    }

    private Directorio buscarDirectorioRecursivo(Directorio actual, String nombre) {
        ListaEnlazada<Directorio> subdirs = actual.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            Directorio dir = subdirs.obtener(i);
            if (dir.getNombre().equals(nombre)) {
                return dir;
            }

            Directorio encontrado = buscarDirectorioRecursivo(dir, nombre);
            if (encontrado != null) {
                return encontrado;
            }
        }

        return null;
    }

    public Archivo buscarArchivoPorBloqueInicial(int bloqueInicial) {
        return buscarArchivoPorBloqueInicialRec(directorioRaiz, bloqueInicial);
    }

    private Archivo buscarArchivoPorBloqueInicialRec(Directorio actual, int bloqueInicial) {
        ListaEnlazada<Archivo> archivos = actual.getArchivos();
        for (int i = 0; i < archivos.getTamano(); i++) {
            Archivo arch = archivos.obtener(i);
            if (arch.getBloqueInicial() == bloqueInicial) {
                return arch;
            }
        }

        ListaEnlazada<Directorio> subdirs = actual.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            Archivo encontrado = buscarArchivoPorBloqueInicialRec(subdirs.obtener(i), bloqueInicial);
            if (encontrado != null) {
                return encontrado;
            }
        }

        return null;
    }

    public boolean crearArchivo(String nombre, int tamaño, String propietario) {
        if (nombre == null || nombre.isBlank() || tamaño <= 0) {
            return false;
        }

        nombre = nombre.trim();

        if (existeNombreGlobal(nombre)) {
            return false;
        }

        if (disco.obtenerEspacioLibre() < tamaño) {
            return false;
        }

        int bloquesAsignados = 0;
        int primerBloque = -1;
        int bloqueAnterior = -1;
        Bloque[] bloquesReales = disco.getBloques();

        for (int i = 0; i < disco.getCapacidad(); i++) {
            if (bloquesReales[i].isLibre()) {
                if (bloquesAsignados == 0) {
                    primerBloque = i;
                } else {
                    bloquesReales[bloqueAnterior].setSiguienteBloque(i);
                }

                bloquesReales[i].setLibre(false);
                bloquesReales[i].setArchivoAsignado(nombre);
                bloquesReales[i].setContenido("Datos de: " + nombre);
                bloquesReales[i].setSiguienteBloque(-1);

                bloqueAnterior = i;
                bloquesAsignados++;
            }

            if (bloquesAsignados == tamaño) {
                break;
            }
        }

        if (primerBloque == -1 || bloquesAsignados != tamaño) {
            return false;
        }

        Archivo nuevoArchivo = new Archivo(nombre, tamaño, primerBloque, propietario);
        directorioRaiz.agregarArchivo(nuevoArchivo);
        return true;
    }

    public boolean crearDirectorio(String nombre, String propietario) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        nombre = nombre.trim();
        if (existeNombreGlobal(nombre)) {
            return false;
        }

        Directorio nuevoDir = new Directorio(nombre, propietario);
        directorioRaiz.agregarSubdirectorio(nuevoDir);
        return true;
    }

    public boolean renombrarItem(String nombreAntiguo, String nombreNuevo) {
        if (nombreAntiguo == null || nombreAntiguo.isBlank() || nombreNuevo == null || nombreNuevo.isBlank()) {
            return false;
        }

        nombreAntiguo = nombreAntiguo.trim();
        nombreNuevo = nombreNuevo.trim();

        if (existeNombreGlobal(nombreNuevo)) {
            return false;
        }

        Archivo arch = buscarArchivoObj(nombreAntiguo);
        if (arch != null) {
            actualizarNombreEnBloques(nombreAntiguo, nombreNuevo);
            arch.setNombre(nombreNuevo);
            return true;
        }

        Directorio dir = buscarDirectorioObj(nombreAntiguo);
        if (dir != null && !"raiz".equalsIgnoreCase(nombreAntiguo)) {
            dir.setNombre(nombreNuevo);
            return true;
        }

        return false;
    }

    public boolean eliminarArchivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        return eliminarArchivoRecursivo(directorioRaiz, nombre.trim());
    }

    private boolean eliminarArchivoRecursivo(Directorio dir, String nombre) {
        ListaEnlazada<Archivo> archivos = dir.getArchivos();

        for (int i = 0; i < archivos.getTamano(); i++) {
            Archivo arch = archivos.obtener(i);
            if (arch.getNombre().equals(nombre)) {
                liberarBloquesArchivo(arch);
                archivos.eliminar(i);
                return true;
            }
        }

        ListaEnlazada<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            if (eliminarArchivoRecursivo(subdirs.obtener(i), nombre)) {
                return true;
            }
        }

        return false;
    }

    public boolean eliminarDirectorio(String nombreDir) {
        if (nombreDir == null || nombreDir.isBlank() || "raiz".equalsIgnoreCase(nombreDir)) {
            return false;
        }
        return eliminarDirectorioRecursivo(directorioRaiz, nombreDir.trim());
    }

    private boolean eliminarDirectorioRecursivo(Directorio padre, String nombreDir) {
        ListaEnlazada<Directorio> subdirs = padre.getSubdirectorios();

        for (int i = 0; i < subdirs.getTamano(); i++) {
            Directorio dir = subdirs.obtener(i);
            if (dir.getNombre().equals(nombreDir)) {
                eliminarContenidoDirectorio(dir);
                subdirs.eliminar(i);
                return true;
            }

            if (eliminarDirectorioRecursivo(dir, nombreDir)) {
                return true;
            }
        }

        return false;
    }

    private void eliminarContenidoDirectorio(Directorio dir) {
        ListaEnlazada<Archivo> archivos = dir.getArchivos();
        while (!archivos.estaVacia()) {
            Archivo arch = archivos.obtener(0);
            liberarBloquesArchivo(arch);
            archivos.eliminar(0);
        }

        ListaEnlazada<Directorio> subdirs = dir.getSubdirectorios();
        while (!subdirs.estaVacia()) {
            Directorio subDir = subdirs.obtener(0);
            eliminarContenidoDirectorio(subDir);
            subdirs.eliminar(0);
        }
    }

    private void liberarBloquesArchivo(Archivo arch) {
        Bloque[] bloquesReales = disco.getBloques();
        int bloqueActual = arch.getBloqueInicial();

        while (bloqueActual != -1) {
            int siguienteBloque = bloquesReales[bloqueActual].getSiguienteBloque();
            bloquesReales[bloqueActual].setLibre(true);
            bloquesReales[bloqueActual].setArchivoAsignado("Ninguno");
            bloquesReales[bloqueActual].setContenido("");
            bloquesReales[bloqueActual].setSiguienteBloque(-1);
            bloqueActual = siguienteBloque;
        }
    }

    private void actualizarNombreEnBloques(String nombreViejo, String nombreNuevo) {
        Bloque[] bloques = disco.getBloques();
        for (int i = 0; i < bloques.length; i++) {
            if (!bloques[i].isLibre() && nombreViejo.equals(bloques[i].getArchivoAsignado())) {
                bloques[i].setArchivoAsignado(nombreNuevo);
                bloques[i].setContenido("Datos de: " + nombreNuevo);
            }
        }
    }

    public String leerArchivo(String nombre) {
        Archivo arch = buscarArchivoObj(nombre);
        if (arch == null) {
            return null;
        }

        Proceso p = new Proceso("P" + contadorProcesos++, "LEER", nombre, arch.getBloqueInicial(), 0);
        p.setEstado("Listo");
        colaProcesos.encolar(p);
        actualizarColaVisual();

        return "⏳ Solicitud enviada a la cola de I/O.\n"
                + "El disco se moverá pronto hacia el bloque " + arch.getBloqueInicial() + ".\n"
                + "Revisa el Log de Procesos.";
    }

    public void encolarSolicitudLectura(String nombreArchivo) {
        Archivo arch = buscarArchivoObj(nombreArchivo);
        if (arch == null) {
            return;
        }

        Proceso p = new Proceso("P" + contadorProcesos++, "LEER", nombreArchivo, arch.getBloqueInicial(), 0);
        p.setEstado("Listo");
        colaProcesos.encolar(p);
        actualizarColaVisual();
    }

    public String encolarSolicitudCreacion(String nombreArchivo, int tamano) {
        Proceso p = new Proceso("P" + contadorProcesos++, "CREAR", nombreArchivo, 0, tamano);
        p.setEstado("Listo");
        colaProcesos.encolar(p);
        actualizarColaVisual();
        return "⏳ Solicitud de CREACIÓN enviada a la cola.";
    }

    public String encolarSolicitudEliminacion(String nombreArchivo) {
        int bloqueDestino = 0;
        Archivo arch = buscarArchivoObj(nombreArchivo);
        if (arch != null) {
            bloqueDestino = arch.getBloqueInicial();
        }

        Proceso p = new Proceso("P" + contadorProcesos++, "ELIMINAR", nombreArchivo, bloqueDestino, 0);
        p.setEstado("Listo");
        colaProcesos.encolar(p);
        actualizarColaVisual();
        return "⏳ Solicitud de ELIMINACIÓN enviada a la cola.";
    }


    public String obtenerEstadisticas() {
        int total = disco.getCapacidad();
        int libre = disco.obtenerEspacioLibre();
        int ocupado = total - libre;

        int numArchivos = contarArchivosRec(directorioRaiz);
        int numCarpetas = contarDirectoriosRec(directorioRaiz) - 1; // no contar raíz

        double porcentajeUso = ((double) ocupado / total) * 100;

        return "📊 ESTADÍSTICAS DEL DISCO 📊\n\n"
                + "🔹 Capacidad Total: " + total + " bloques\n"
                + "🔴 Espacio Ocupado: " + ocupado + " bloques (" + String.format("%.1f", porcentajeUso) + "%)\n"
                + "🟢 Espacio Libre: " + libre + " bloques\n"
                + "📄 Total de Archivos: " + numArchivos + "\n"
                + "📁 Total de Carpetas: " + numCarpetas;
    }

    private int contarArchivosRec(Directorio dir) {
        int total = dir.getArchivos().getTamano();
        ListaEnlazada<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            total += contarArchivosRec(subdirs.obtener(i));
        }
        return total;
    }

    private int contarDirectoriosRec(Directorio dir) {
        int total = 1;
        ListaEnlazada<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            total += contarDirectoriosRec(subdirs.obtener(i));
        }
        return total;
    }

    // =========================================================
    // JSON
    // =========================================================

    private void agregarArchivosAlJsonRecursivo(Directorio dir, JsonObject systemFiles) {
        ListaEnlazada<Archivo> archivos = dir.getArchivos();
        for (int i = 0; i < archivos.getTamano(); i++) {
            Archivo arch = archivos.obtener(i);
            JsonObject fileObj = new JsonObject();
            fileObj.addProperty("name", arch.getNombre());
            fileObj.addProperty("blocks", arch.getTamañoEnBloques());
            systemFiles.add(String.valueOf(arch.getBloqueInicial()), fileObj);
        }

        ListaEnlazada<Directorio> subdirs = dir.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            agregarArchivosAlJsonRecursivo(subdirs.obtener(i), systemFiles);
        }
    }

    public String exportarAJson(String rutaArchivo) {
        try {
            JsonObject raiz = new JsonObject();
            raiz.addProperty("initial_head", this.posicionCabeza);

            JsonObject systemFiles = new JsonObject();
            agregarArchivosAlJsonRecursivo(directorioRaiz, systemFiles);
            raiz.add("system_files", systemFiles);

            raiz.add("requests", new JsonArray());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(rutaArchivo)) {
                gson.toJson(raiz, writer);
            }

            return "✅ Estado guardado correctamente en:\n" + rutaArchivo;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al guardar JSON: " + e.getMessage();
        }
    }

    private void reiniciarEstadoLogico() {
        this.disco = new Disco(100);
        this.directorioRaiz = new Directorio("raiz");
        this.colaProcesos = new Cola<>();
        this.journal = new ListaEnlazada<>();
        this.simularFallo = false;
    }

    private String resolverNombreDesdePos(int pos) {
        Archivo arch = buscarArchivoPorBloqueInicial(pos);
        return (arch != null) ? arch.getNombre() : null;
    }

    public String importarDeJson(String rutaArchivo) {
        try (FileReader reader = new FileReader(rutaArchivo)) {
            reiniciarEstadoLogico();

            JsonObject raiz = JsonParser.parseReader(reader).getAsJsonObject();

            if (raiz.has("initial_head")) {
                this.posicionCabeza = raiz.get("initial_head").getAsInt();
                imprimirEnLogVisual("📍 Cabezal movido a la posición: " + posicionCabeza);
            }

            if (raiz.has("system_files")) {
                JsonObject sysFiles = raiz.getAsJsonObject("system_files");
                for (String key : sysFiles.keySet()) {
                    JsonObject fileObj = sysFiles.getAsJsonObject(key);
                    String name = fileObj.get("name").getAsString();
                    int blocks = fileObj.get("blocks").getAsInt();
                    int bloqueInicial = Integer.parseInt(key);
                    this.crearArchivoForzado(name, blocks, bloqueInicial, "admin");
                }
                imprimirEnLogVisual("📂 System Files cargados correctamente.");
            }

            if (raiz.has("requests")) {
                JsonArray requests = raiz.getAsJsonArray("requests");
                for (JsonElement req : requests) {
                    JsonObject reqObj = req.getAsJsonObject();
                    String op = reqObj.get("op").getAsString().toUpperCase();

                    int pos = reqObj.has("pos") ? reqObj.get("pos").getAsInt() : 0;
                    String nombreArchivo = reqObj.has("name")
                            ? reqObj.get("name").getAsString()
                            : resolverNombreDesdePos(pos);

                    if (op.equals("CREATE")) {
                        int size = reqObj.has("size") ? reqObj.get("size").getAsInt() : 1;
                        if (nombreArchivo == null || nombreArchivo.isBlank()) {
                            nombreArchivo = "nuevo_" + contadorProcesos + ".txt";
                        }
                        encolarSolicitudCreacion(nombreArchivo, size);

                    } else if (op.equals("DELETE")) {
                        if (nombreArchivo != null) {
                            encolarSolicitudEliminacion(nombreArchivo);
                        } else {
                            imprimirEnLogVisual("⚠️ DELETE ignorado: no se encontró archivo para pos=" + pos);
                        }

                    } else if (op.equals("READ")) {
                        if (nombreArchivo != null) {
                            encolarSolicitudLectura(nombreArchivo);
                        } else {
                            imprimirEnLogVisual("⚠️ READ ignorado: no se encontró archivo para pos=" + pos);
                        }

                    } else if (op.equals("UPDATE")) {
                        if (nombreArchivo != null) {
                            // El Planificador actual no ejecuta UPDATE real todavía.
                            Proceso p = new Proceso("P" + contadorProcesos++, "UPDATE", nombreArchivo, pos, 0);
                            p.setEstado("Listo");
                            colaProcesos.encolar(p);
                            imprimirEnLogVisual("⚠️ UPDATE cargado en cola para '" + nombreArchivo + "'. Requiere Planificador corregido para aplicar el cambio real.");
                        } else {
                            imprimirEnLogVisual("⚠️ UPDATE ignorado: no se encontró archivo para pos=" + pos);
                        }
                    }
                }
                actualizarColaVisual();
            }

            refrescarPantallaCompleta();
            return "✅ JSON de prueba cargado e inicializado.";

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al leer el JSON: Verifica la estructura.\nDetalle: " + e.getMessage();
        }
    }


    public void recuperarSistemaDespuesDeFallo() {
        imprimirEnLogVisual("🔄 INICIANDO RECUPERACIÓN DEL SISTEMA (JOURNALING)...");

        for (int i = 0; i < journal.getTamano(); i++) {
            String log = journal.obtener(i);

            if (log.startsWith("PENDIENTE: CREAR")) {
                String nombreArchivo = log.replace("PENDIENTE: CREAR ", "").trim();

                boolean confirmado = false;
                for (int j = i + 1; j < journal.getTamano(); j++) {
                    if (journal.obtener(j).equals("CONFIRMADA: CREAR " + nombreArchivo)) {
                        confirmado = true;
                        break;
                    }
                }

                if (!confirmado) {
                    imprimirEnLogVisual("⚠️ Detectada creación incompleta de '" + nombreArchivo + "'. Aplicando UNDO...");
                    boolean borrado = eliminarArchivo(nombreArchivo);
                    if (borrado) {
                        imprimirEnLogVisual("✅ UNDO Exitoso: Bloques basura liberados.");
                    }
                }
            }
        }

        this.simularFallo = false;
        this.journal = new ListaEnlazada<>();

        this.planificador = new Planificador(this);
        this.planificador.start();

        imprimirEnLogVisual("🚀 Sistema recuperado y en línea.");
        refrescarPantallaCompleta();
    }


    public boolean crearArchivoForzado(String nombre, int tamano, int bloqueInicial, String propietario) {
        if (nombre == null || nombre.isBlank() || tamano <= 0 || bloqueInicial < 0) {
            return false;
        }

        Bloque[] bloquesReales = disco.getBloques();

        if (bloqueInicial + tamano > bloquesReales.length) {
            return false;
        }

        for (int i = 0; i < tamano; i++) {
            int actual = bloqueInicial + i;
            if (!bloquesReales[actual].isLibre()) {
                return false;
            }
        }

        int bloqueAnterior = -1;
        for (int i = 0; i < tamano; i++) {
            int actual = bloqueInicial + i;

            if (bloqueAnterior != -1) {
                bloquesReales[bloqueAnterior].setSiguienteBloque(actual);
            }

            bloquesReales[actual].setLibre(false);
            bloquesReales[actual].setArchivoAsignado(nombre);
            bloquesReales[actual].setContenido("Datos sistema: " + nombre);
            bloquesReales[actual].setSiguienteBloque(-1);

            bloqueAnterior = actual;
        }

        Archivo nuevoArchivo = new Archivo(nombre, tamano, bloqueInicial, propietario);
        directorioRaiz.agregarArchivo(nuevoArchivo);
        return true;
    }
}
