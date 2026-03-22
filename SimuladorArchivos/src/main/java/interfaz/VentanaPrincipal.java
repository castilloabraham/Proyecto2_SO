package interfaz;

import controlador.GestorArchivos;
import modelo.Bloque;
import modelo.Disco;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    // --- Colores Modernos para el Estilo ---
    private final Color COLOR_FONDO = new Color(45, 50, 60);
    private final Color COLOR_PANEL = new Color(55, 60, 70); 
    private final Color COLOR_TEXTO = Color.WHITE;
    private final Color COLOR_ACCENTO = new Color(100, 180, 240);
    private final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 14);

    // --- EL CEREBRO DE LA APLICACIÓN ---
    private GestorArchivos gestor;

    // --- Componentes visuales ---
    private JTree arbolDirectorios;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;
    private JTextArea areaLog;
    private JTextArea areaProcesos;
    private JPanel panelDiscoBlocks;
    private JSlider sliderVelocidad;

    public VentanaPrincipal() {
        super("Simulador de Sistema de Archivos OS - [MODERNO]");
        
        // 1. INICIALIZAMOS EL CONTROLADOR (El cerebro)
        this.gestor = new GestorArchivos();
        this.gestor.setVentana(this);

        setSize(1300, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(5, 5));

        // --- Inicialización Modular de Secciones ---
        add(crearPanelControles(), BorderLayout.NORTH); 
        
        JPanel panelCentralYBottom = new JPanel(new BorderLayout(5, 5));
        panelCentralYBottom.setOpaque(false);
        panelCentralYBottom.add(crearPanelSistemaArchivos(), BorderLayout.WEST); 
        panelCentralYBottom.add(crearPanelTabsCentrales(), BorderLayout.CENTER);   
        panelCentralYBottom.add(crearPanelLogsProcesos(), BorderLayout.SOUTH);   
        
        add(panelCentralYBottom, BorderLayout.CENTER);
    }

    private JPanel crearPanelControles() {
        JPanel panelControles = crearPanelBase("Controles");
        panelControles.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        panelControles.add(crearSeccionModoPlanificador(), gbc);
        
        gbc.gridx = 1;
        panelControles.add(crearSeccionBotonesAccion(), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panelControles.add(crearSeccionVelocidadStatus(), gbc);
        
        return panelControles;
    }

    private JPanel crearSeccionModoPlanificador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        
        panel.add(new JLabel("Modo:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        JRadioButton rbAdmin = new JRadioButton("Administrador", true) {{ setForeground(COLOR_TEXTO); setOpaque(false); }};
        JRadioButton rbUsuario = new JRadioButton("Usuario") {{ setForeground(COLOR_TEXTO); setOpaque(false); }};
        ButtonGroup bgModo = new ButtonGroup(); bgModo.add(rbAdmin); bgModo.add(rbUsuario);
        panel.add(rbAdmin); panel.add(rbUsuario);

        panel.add(new JLabel("Planificador:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        String[] algos = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        
        // --- MODIFICACIÓN AQUÍ ---
        JComboBox<String> comboAlgo = new JComboBox<>(algos);
        comboAlgo.addActionListener(e -> {
            String seleccion = (String) comboAlgo.getSelectedItem();
            gestor.cambiarPoliticaPlanificador(seleccion); // Le avisamos al cerebro
            areaLog.append("⚙️ Política cambiada a: " + seleccion + "\n");
        });
        // -------------------------
        
        panel.add(comboAlgo);
        
        return panel;
    }

    private JPanel crearSeccionBotonesAccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        
        // --- 1. BOTÓN CREAR ARCHIVO ---
        JButton btnCrearArchivo = new JButton("Crear Archivo");
        styleModernButton(btnCrearArchivo);
        btnCrearArchivo.addActionListener(e -> accionCrearArchivo());
        panel.add(btnCrearArchivo);

        // --- 2. BOTÓN CREAR DIRECTORIO ---
        JButton btnCrearDirectorio = new JButton("Crear Directorio");
        styleModernButton(btnCrearDirectorio);
        btnCrearDirectorio.addActionListener(e -> accionCrearDirectorio());
        panel.add(btnCrearDirectorio);

        // --- 3. BOTÓN RENOMBRAR ---
        JButton btnRenombrar = new JButton("Renombrar");
        styleModernButton(btnRenombrar);
        btnRenombrar.addActionListener(e -> accionRenombrar());
        panel.add(btnRenombrar);

        // --- 4. BOTÓN ELIMINAR ---
        JButton btnEliminar = new JButton("Eliminar");
        styleModernButton(btnEliminar);
        btnEliminar.addActionListener(e -> accionEliminarArchivo());
        panel.add(btnEliminar);

        // --- 5. BOTÓN ESTADÍSTICAS ---
        JButton btnEstadisticas = new JButton("Estadísticas");
        styleModernButton(btnEstadisticas);
        btnEstadisticas.addActionListener(e -> accionEstadisticas()); 
        panel.add(btnEstadisticas);

        JButton btnLeer = new JButton("Leer");
        styleModernButton(btnLeer);
        btnLeer.addActionListener(e -> accionLeerArchivo()); 
        panel.add(btnLeer);
        
        return panel;
    }

    private JPanel crearSeccionVelocidadStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        
        panel.add(new JLabel("Velocidad:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        
        // 1. Creamos la etiqueta ANTES del slider para poder modificarla
        JLabel lblVelocidad = new JLabel("300 ms");
        lblVelocidad.setForeground(COLOR_TEXTO);
        
        // 2. Configuramos el slider: Min=100ms, Max=2000ms, Inicio=300ms
        sliderVelocidad = new JSlider(100, 10000, 5000);
        sliderVelocidad.setOpaque(false);
        sliderVelocidad.setForeground(COLOR_ACCENTO);
        
        // 3. Le agregamos el "escuchador" al slider
        sliderVelocidad.addChangeListener(e -> {
            int valorMs = sliderVelocidad.getValue();
            lblVelocidad.setText(valorMs + " ms"); // ¡Esto actualiza el texto en la pantalla!
            gestor.cambiarVelocidadDisco(valorMs); // Le avisamos al cerebro
        });

        panel.add(sliderVelocidad);
        panel.add(lblVelocidad);

        panel.add(new JLabel("   Ciclo: 0") {{ setForeground(COLOR_TEXTO); }});
        panel.add(new JLabel("   Cabeza: 0") {{ setForeground(COLOR_TEXTO); }});
        
        return panel;
    }

    private JPanel crearPanelSistemaArchivos() {
        JPanel panel = crearPanelBase("Sistema de Archivos");
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setLayout(new BorderLayout());

        // 2. CONECTAMOS EL ÁRBOL CON LA CARPETA REAL DEL GESTOR
        String nombreRaiz = gestor.getDirectorioRaiz().getNombre();
        DefaultMutableTreeNode raizNode = new DefaultMutableTreeNode(nombreRaiz);

        arbolDirectorios = new JTree(new DefaultTreeModel(raizNode));
        arbolDirectorios.setBackground(COLOR_PANEL);
        arbolDirectorios.setForeground(COLOR_TEXTO);
        
        JScrollPane scroll = new JScrollPane(arbolDirectorios);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    private JTabbedPane crearPanelTabsCentrales() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        styleModernTabs(tabs);

        tabs.addTab("Simulación de Disco", crearPanelDiscoMap());
        tabs.addTab("Tabla de Asignación", crearPanelTablaAsignacion());
        tabs.addTab("Caché", crearPanelCostoCache());
        
        return tabs;
    }

    private JPanel crearPanelDiscoMap() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 3. CONECTAMOS EL MAPA DEL DISCO CON LOS BLOQUES REALES DEL GESTOR
        Disco discoReal = gestor.getDisco();
        int totalBloques = discoReal.getCapacidad();
        Bloque[] bloquesReales = discoReal.getBloques();

        int cols = 10;
        int rows = (int) Math.ceil((double) totalBloques / cols);
        
        panelDiscoBlocks = new JPanel(new GridLayout(rows, cols, 3, 3));
        panelDiscoBlocks.setOpaque(false);
        
        // Dibujamos cada bloque real
        for (int i = 0; i < totalBloques; i++) {
            JPanel block = new JPanel();
            
            // Si el bloque está libre lo pintamos gris, sino rojo (por ahora)
            if (bloquesReales[i].isLibre()) {
                block.setBackground(Color.GRAY);
            } else {
                block.setBackground(Color.RED); 
            }
            
            block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
            
            JLabel lblId = new JLabel(String.valueOf(bloquesReales[i].getId()));
            lblId.setFont(new Font("Arial", Font.PLAIN, 10));
            lblId.setForeground(Color.BLACK);
            block.add(lblId);
            
            panelDiscoBlocks.add(block);
        }
        
        JScrollPane scrollDisco = new JScrollPane(panelDiscoBlocks);
        scrollDisco.setOpaque(false);
        scrollDisco.getViewport().setOpaque(false);
        scrollDisco.setBorder(null);
        panel.add(scrollDisco, BorderLayout.CENTER);

        // Mostramos el espacio libre real
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelStatus.setOpaque(false);
        panelStatus.add(new JLabel("Bloques libres: " + discoReal.obtenerEspacioLibre() + "/" + totalBloques) {{ setForeground(COLOR_TEXTO); }});
        panel.add(panelStatus, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelTablaAsignacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Nombre del Archivo", "Dueño", "Tamaño (Bloques)", "Bloque Inicial"};
        modeloTabla = new DefaultTableModel(null, columnas);
        tablaArchivos = new JTable(modeloTabla);
        styleModernTable(tablaArchivos);

        panel.add(new JScrollPane(tablaArchivos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelCostoCache() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new JLabel("Aquí se visualizará el costo de acceso al Caché...", JLabel.CENTER) {{ setForeground(COLOR_TEXTO); }});
        return panel;
    }

    private JPanel crearPanelLogsProcesos() {
        JPanel panelLogsProcesos = new JPanel(new GridLayout(1, 2, 5, 0));
        panelLogsProcesos.setOpaque(false);
        panelLogsProcesos.setPreferredSize(new Dimension(0, 250));

        JPanel panelLog = crearPanelBase("Log de Eventos");
        panelLog.setLayout(new BorderLayout());
        areaLog = crearTextAreaModerna();
        areaLog.append("Sistema iniciado correctamente...\n");
        panelLog.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        
        JButton btnLimpiarLog = new JButton("Limpiar Log");
        styleModernButton(btnLimpiarLog);
        panelLog.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)) {{ setOpaque(false); add(btnLimpiarLog); }}, BorderLayout.SOUTH);
        panelLogsProcesos.add(panelLog);

        JPanel panelProcesos = crearPanelBase("Cola de Procesos");
        panelProcesos.setLayout(new BorderLayout());
        areaProcesos = crearTextAreaModerna();
        areaProcesos.append("=== LISTOS ===\n=== EN CPU ===\n=== BLOQUEADOS ===\n=== I/O EN EJECUCIÓN ===\n=== COLA I/O ===\n");
        panelProcesos.add(new JScrollPane(areaProcesos), BorderLayout.CENTER);
        panelLogsProcesos.add(panelProcesos);

        return panelLogsProcesos;
    }

    private JPanel crearPanelBase(String titulo) {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_PANEL);
        Border baseBorder = BorderFactory.createLineBorder(COLOR_FONDO, 2, true);
        TitledBorder titleBorder = BorderFactory.createTitledBorder(baseBorder, titulo);
        titleBorder.setTitleFont(FUENTE_TITULO);
        titleBorder.setTitleColor(COLOR_TEXTO);
        panel.setBorder(titleBorder);
        return panel;
    }

    private JTextArea crearTextAreaModerna() {
        JTextArea area = new JTextArea();
        area.setBackground(COLOR_PANEL);
        area.setForeground(new Color(200, 200, 200));
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        return area;
    }

    private void styleModernButton(JButton btn) {
        btn.setBackground(COLOR_ACCENTO);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void styleModernTabs(JTabbedPane tabs) {
        UIManager.put("TabbedPane.background", COLOR_FONDO);
        UIManager.put("TabbedPane.foreground", COLOR_TEXTO);
        UIManager.put("TabbedPane.selected", COLOR_PANEL);
        UIManager.put("TabbedPane.contentAreaColor", COLOR_PANEL);
        tabs.updateUI();
    }

    private void styleModernTable(JTable table) {
        table.setBackground(COLOR_PANEL);
        table.setForeground(COLOR_TEXTO);
        table.setGridColor(COLOR_FONDO);
        table.getTableHeader().setBackground(COLOR_PANEL);
        table.getTableHeader().setForeground(COLOR_TEXTO);
        table.getTableHeader().setFont(FUENTE_TITULO);
    }
    
    public void agregarMensajeLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength()); // Auto-scroll
        });
    }

    public void actualizarPantallaProcesos(String texto) {
        SwingUtilities.invokeLater(() -> {
            areaProcesos.setText(texto);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
    // --- LÓGICA DE EVENTOS (ACCIONES) ---
    private void accionCrearArchivo() {
        // 1. Pedir datos al usuario con ventanitas (Dialogs)
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo (ej. documento.txt):");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String strTamano = JOptionPane.showInputDialog(this, "Ingrese el tamaño en bloques (ej. 5):");
        if (strTamano == null || strTamano.trim().isEmpty()) return;

        try {
            int tamano = Integer.parseInt(strTamano);
            
            // 2. Llamar al Gestor (Cerebro)
            boolean exito = gestor.crearArchivo(nombre, tamano, "admin");
            
            if (exito) {
                JOptionPane.showMessageDialog(this, "Archivo creado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                areaLog.append("Archivo creado: " + nombre + " (" + tamano + " bloques)\n");
                actualizarPantallaCompleta(); // 3. Refrescar los dibujos
            } else {
                JOptionPane.showMessageDialog(this, "No hay espacio suficiente en el disco.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El tamaño debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Este método vuelve a dibujar el disco y la tabla para mostrar los cambios
    private void actualizarPantallaCompleta() {
        // --- 1. Refrescar Mapa de Disco ---
        panelDiscoBlocks.removeAll();
        modelo.Bloque[] bloquesReales = gestor.getDisco().getBloques();
        
        for (int i = 0; i < gestor.getDisco().getCapacidad(); i++) {
            JPanel block = new JPanel();
            block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
            
            // LÓGICA DE COLOR Y TOOLTIP (HOVER)
            if (bloquesReales[i].isLibre()) {
                block.setBackground(Color.GRAY);
                block.setToolTipText("Bloque " + i + ": Libre"); // Hover gris
            } else {
                block.setBackground(COLOR_ACCENTO);
                block.setToolTipText("Bloque " + i + ": Ocupado por '" + bloquesReales[i].getArchivoAsignado() + "'"); // Hover azul
            }
            
            JLabel lblId = new JLabel(String.valueOf(i));
            lblId.setFont(new Font("Arial", Font.PLAIN, 10));
            lblId.setForeground(Color.BLACK);
            block.add(lblId);
            panelDiscoBlocks.add(block);
        }
        panelDiscoBlocks.revalidate();
        panelDiscoBlocks.repaint();

        // --- 2. Refrescar Tabla de Asignación ---
        modeloTabla.setRowCount(0); // Limpiar tabla vieja
        
        modelo.Directorio raiz = gestor.getDirectorioRaiz();
        estructuras.ListaEnlazada<modelo.Archivo> archivos = raiz.getArchivos();
        
        // Recorremos tu ListaEnlazada usando tus métodos
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            // Agregamos una fila a la tabla por cada archivo
            modeloTabla.addRow(new Object[]{
                arch.getNombre(), 
                arch.getPropietario(), 
                arch.getTamañoEnBloques(), 
                arch.getBloqueInicial()
            });
        }

        // --- 3. Refrescar Árbol de Directorios (JTree) ---
        DefaultMutableTreeNode raizNode = (DefaultMutableTreeNode) arbolDirectorios.getModel().getRoot();
        raizNode.removeAllChildren(); // Limpiar viejo
        
        // 3.1 Primero agregamos las subcarpetas al árbol
        estructuras.ListaEnlazada<modelo.Directorio> subdirs = raiz.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            modelo.Directorio dir = subdirs.obtener(i);
            raizNode.add(new DefaultMutableTreeNode("📁 " + dir.getNombre())); // Ícono de carpeta
        }

        // 3.2 Luego agregamos los archivos al árbol
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            raizNode.add(new DefaultMutableTreeNode("📄 " + arch.getNombre() + " [" + arch.getTamañoEnBloques() + " blk]")); // Ícono de archivo
        }
        
        ((DefaultTreeModel) arbolDirectorios.getModel()).reload();
        
        // Expandir el árbol automáticamente para que siempre se vea lo que agregamos
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
            arbolDirectorios.expandRow(i);
        }
    }
    
    private void accionEliminarArchivo() {
        // 1. Preguntarle al usuario qué archivo quiere borrar
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo a eliminar:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        // 2. Llamar al Gestor para que lo elimine
        boolean exito = gestor.eliminarArchivo(nombre);

        // 3. Mostrar resultado y refrescar
        if (exito) {
            JOptionPane.showMessageDialog(this, "Archivo '" + nombre + "' eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            areaLog.append("Archivo eliminado: " + nombre + "\n");
            actualizarPantallaCompleta(); // ¡La magia de redibujar todo!
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo '" + nombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionCrearDirectorio() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        gestor.crearDirectorio(nombre);
        areaLog.append("Directorio creado: " + nombre + "\n");
        actualizarPantallaCompleta();
    }
    private void accionRenombrar() {
        // Pedimos el nombre actual
        String nombreAntiguo = JOptionPane.showInputDialog(this, "Ingrese el nombre ACTUAL del archivo o carpeta:");
        if (nombreAntiguo == null || nombreAntiguo.trim().isEmpty()) return;

        // Pedimos el nombre nuevo
        String nombreNuevo = JOptionPane.showInputDialog(this, "Ingrese el NUEVO nombre:");
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) return;

        // Le decimos al Gestor que haga el trabajo
        boolean exito = gestor.renombrarItem(nombreAntiguo, nombreNuevo);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Renombrado con éxito a: " + nombreNuevo, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            areaLog.append("Renombrado: '" + nombreAntiguo + "' -> '" + nombreNuevo + "'\n");
            actualizarPantallaCompleta(); // Refrescamos el árbol y la tabla
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró nada con el nombre '" + nombreAntiguo + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionEstadisticas() {
        // Pedimos los datos calculados al Gestor
        String reporte = gestor.obtenerEstadisticas();
        
        // Lo mostramos en una ventana emergente
        JOptionPane.showMessageDialog(this, reporte, "Estadísticas del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void accionLeerArchivo() {
        // 1. Pedimos qué archivo quiere leer
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo a leer:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        // 2. Le pedimos al Gestor que lo lea y calcule el movimiento
        String resultado = gestor.leerArchivo(nombre);

        // 3. Mostramos los resultados
        if (resultado != null) {
            JOptionPane.showMessageDialog(this, resultado, "Lectura de Disco", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo '" + nombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}