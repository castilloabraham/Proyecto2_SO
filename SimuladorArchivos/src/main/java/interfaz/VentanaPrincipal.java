package interfaz;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    // --- Colores Modernos para el Estilo ---
    private final Color COLOR_FONDO = new Color(45, 50, 60); // Gris oscuro azulado
    private final Color COLOR_PANEL = new Color(55, 60, 70);  // Un poco más claro para los paneles
    private final Color COLOR_TEXTO = Color.WHITE;
    private final Color COLOR_ACCENTO = new Color(100, 180, 240); // Azul claro
    private final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 14);

    // --- Componentes para guardar referencias más adelante ---
    private JTree arbolDirectorios;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;
    private JTextArea areaLog;
    private JTextArea areaProcesos;
    private JPanel panelDiscoBlocks;
    private JSlider sliderVelocidad;

    public VentanaPrincipal() {
        super("Simulador de Sistema de Archivos OS - [MODERNO]");
        setSize(1300, 900); // Un tamaño amplio para que quepa todo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(5, 5)); // BorderLayout principal con espacios

        // --- Inicialización Modular de Secciones ---
        add(crearPanelControles(), BorderLayout.NORTH); // 1. Sección de Controles (Arriba)
        
        JPanel panelCentralYBottom = new JPanel(new BorderLayout(5, 5));
        panelCentralYBottom.setOpaque(false);
        panelCentralYBottom.add(crearPanelSistemaArchivos(), BorderLayout.WEST); // 2. Explorador de Archivos (Centro-Izquierda)
        panelCentralYBottom.add(crearPanelTabsCentrales(), BorderLayout.CENTER);   // 3. Tabs con Disco/Tabla (Centro)
        panelCentralYBottom.add(crearPanelLogsProcesos(), BorderLayout.SOUTH);   // 4. Logs y Procesos (Abajo)
        
        add(panelCentralYBottom, BorderLayout.CENTER);
    }

    /** 1. Sección de Controles (Arriba) **/
    private JPanel crearPanelControles() {
        JPanel panelControles = crearPanelBase("Controles");
        panelControles.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Fila 1: Modo, Planificador, Botones de Acción
        gbc.gridx = 0; gbc.gridy = 0;
        panelControles.add(crearSeccionModoPlanificador(), gbc);
        
        gbc.gridx = 1;
        panelControles.add(crearSeccionBotonesAccion(), gbc);

        // Fila 2: Velocidad, Status
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; // Ocupa ambas columnas
        panelControles.add(crearSeccionVelocidadStatus(), gbc);
        
        return panelControles;
    }

    private JPanel crearSeccionModoPlanificador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        
        // Modo
        panel.add(new JLabel("Modo:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        JRadioButton rbAdmin = new JRadioButton("Administrador", true) {{ setForeground(COLOR_TEXTO); setOpaque(false); }};
        JRadioButton rbUsuario = new JRadioButton("Usuario") {{ setForeground(COLOR_TEXTO); setOpaque(false); }};
        ButtonGroup bgModo = new ButtonGroup(); bgModo.add(rbAdmin); bgModo.add(rbUsuario);
        panel.add(rbAdmin); panel.add(rbUsuario);

        // Planificador
        panel.add(new JLabel("Planificador:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        String[] algos = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        JComboBox<String> comboAlgo = new JComboBox<>(algos);
        panel.add(comboAlgo);
        
        return panel;
    }

    private JPanel crearSeccionBotonesAccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        
        String[] nombresBotones = {"Crear Archivo", "Crear Directorio", "Leer", "Renombrar", "Eliminar", "Estadísticas", "Pausar"};
        for (String nombre : nombresBotones) {
            JButton btn = new JButton(nombre);
            styleModernButton(btn); // Le damos estilo moderno
            panel.add(btn);
        }
        return panel;
    }

    private JPanel crearSeccionVelocidadStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        
        // Velocidad con slider
        panel.add(new JLabel("Velocidad:", JLabel.LEFT) {{ setForeground(COLOR_TEXTO); }});
        sliderVelocidad = new JSlider(0, 100, 50);
        sliderVelocidad.setOpaque(false);
        sliderVelocidad.setForeground(COLOR_ACCENTO);
        panel.add(sliderVelocidad);
        panel.add(new JLabel("300 ms (Normal)") {{ setForeground(COLOR_TEXTO); }});

        // Status Labels
        panel.add(new JLabel("   Ciclo: 917") {{ setForeground(COLOR_TEXTO); }});
        panel.add(new JLabel("   Cabeza: 8") {{ setForeground(COLOR_TEXTO); }});
        
        return panel;
    }

    /** 2. Explorador de Archivos (Centro-Izquierda) **/
    private JPanel crearPanelSistemaArchivos() {
        JPanel panel = crearPanelBase("Sistema de Archivos");
        panel.setPreferredSize(new Dimension(300, 0)); // Ancho fijo
        panel.setLayout(new BorderLayout());

        // Creamos un árbol de ejemplo rápido
        DefaultMutableTreeNode raizNode = new DefaultMutableTreeNode("raíz");
        DefaultMutableTreeNode docNode = new DefaultMutableTreeNode("documentos");
        docNode.add(new DefaultMutableTreeNode("informe1 [5 bloques]"));
        docNode.add(new DefaultMutableTreeNode("nota [2 bloques]"));
        raizNode.add(docNode);
        raizNode.add(new DefaultMutableTreeNode("imágenes"));
        raizNode.add(new DefaultMutableTreeNode("proyectos"));

        arbolDirectorios = new JTree(new DefaultTreeModel(raizNode));
        arbolDirectorios.setBackground(COLOR_PANEL); // Fondo del panel
        arbolDirectorios.setForeground(COLOR_TEXTO); // Texto
        
        // Un poco de estilo para el JTree
        JScrollPane scroll = new JScrollPane(arbolDirectorios);
        scroll.setBorder(null); // Sin borde para el scroll interno
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    /** 3. Tabs con Disco/Tabla (Centro) **/
    private JTabbedPane crearPanelTabsCentrales() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        styleModernTabs(tabs); // Estilo moderno para los tabs

        tabs.addTab("Simulación de Disco", crearPanelDiscoMap());
        tabs.addTab("Tabla de Asignación", crearPanelTablaAsignacion());
        tabs.addTab("Caché", crearPanelCostoCache());
        
        return tabs;
    }

    private JPanel crearPanelDiscoMap() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // El mapa del disco con GridLayout (ej. 10x10 = 100 bloques)
        int rows = 10, cols = 10;
        panelDiscoBlocks = new JPanel(new GridLayout(rows, cols, 3, 3)); // 3px gap
        panelDiscoBlocks.setOpaque(false);
        
        // Creamos bloques de ejemplo como en la imagen
        Color[] coloresEjemplo = {
            Color.RED, Color.GREEN, Color.BLUE, new Color(150, 50, 200), // Púrpura
            Color.YELLOW, Color.CYAN, Color.ORANGE, Color.GRAY // Libres
        };
        
        for (int i = 0; i < (rows * cols); i++) {
            JPanel block = new JPanel();
            Color colorB;
            if (i < 4) colorB = coloresEjemplo[0]; // Rojo
            else if (i < 10) colorB = coloresEjemplo[1]; // Verde
            else if (i < 20) colorB = coloresEjemplo[2]; // Azul
            else if (i < 30) colorB = coloresEjemplo[3]; // Púrpura
            else if (i < 40) colorB = coloresEjemplo[4]; // Amarillo
            else if (i < 50) colorB = coloresEjemplo[5]; // Cyan
            else if (i < 60) colorB = coloresEjemplo[6]; // Naranja
            else colorB = coloresEjemplo[7]; // Libres (Gris)
            
            block.setBackground(colorB);
            block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1)); // Borde limpio
            
            JLabel lblId = new JLabel(String.valueOf(i));
            lblId.setFont(new Font("Arial", Font.PLAIN, 10));
            lblId.setForeground(Color.BLACK);
            block.add(lblId);
            
            panelDiscoBlocks.add(block);
        }
        
        // Envolvemos el mapa en un ScrollPane por si el disco es grande
        JScrollPane scrollDisco = new JScrollPane(panelDiscoBlocks);
        scrollDisco.setOpaque(false);
        scrollDisco.getViewport().setOpaque(false);
        scrollDisco.setBorder(null);
        panel.add(scrollDisco, BorderLayout.CENTER);

        // Status abajo del disco
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelStatus.setOpaque(false);
        panelStatus.add(new JLabel("Bloques libres: 83/100") {{ setForeground(COLOR_TEXTO); }});
        panel.add(panelStatus, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelTablaAsignacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Modelo de la tabla como en el documento
        String[] columnas = {"Nombre del Archivo", "Dueño", "Tamaño (Bloques)", "Bloque Inicial"};
        modeloTabla = new DefaultTableModel(null, columnas);
        tablaArchivos = new JTable(modeloTabla);
        styleModernTable(tablaArchivos); // Estilo moderno

        // Datos de ejemplo rápido
        modeloTabla.addRow(new Object[]{"informe1.txt", "admin", "5", "0"});
        modeloTabla.addRow(new Object[]{"nota.pdf", "usuario1", "2", "4"});

        panel.add(new JScrollPane(tablaArchivos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelCostoCache() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new JLabel("Aquí se visualizará el costo de acceso al Caché...", JLabel.CENTER) {{ setForeground(COLOR_TEXTO); }});
        return panel;
    }

    /** 4. Logs y Procesos (Abajo) **/
    private JPanel crearPanelLogsProcesos() {
        JPanel panelLogsProcesos = new JPanel(new GridLayout(1, 2, 5, 0)); // 2 columnas, espacios
        panelLogsProcesos.setOpaque(false);
        panelLogsProcesos.setPreferredSize(new Dimension(0, 250)); // Altura fija

        // Log de Eventos
        JPanel panelLog = crearPanelBase("Log de Eventos");
        panelLog.setLayout(new BorderLayout());
        areaLog = crearTextAreaModerna();
        areaLog.append("[Ciclo 917] I/O Completada: RENOMBRAR notas.txt -> nota (Bloque 0)\n");
        areaLog.append("[Ciclo 917] Proceso terminado: Proc-15-ACTUALIZAR_ARCHIVO\n");
        panelLog.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        
        JButton btnLimpiarLog = new JButton("Limpiar Log");
        styleModernButton(btnLimpiarLog);
        panelLog.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)) {{ setOpaque(false); add(btnLimpiarLog); }}, BorderLayout.SOUTH);
        panelLogsProcesos.add(panelLog);

        // Cola de Procesos
        JPanel panelProcesos = crearPanelBase("Cola de Procesos");
        panelProcesos.setLayout(new BorderLayout());
        areaProcesos = crearTextAreaModerna();
        areaProcesos.append("=== LISTOS ===\n=== EN CPU ===\n=== BLOQUEADOS ===\n=== I/O EN EJECUCIÓN ===\n=== COLA I/O ===\n");
        panelProcesos.add(new JScrollPane(areaProcesos), BorderLayout.CENTER);
        panelLogsProcesos.add(panelProcesos);

        return panelLogsProcesos;
    }

    // --- MÉTODOS DE AYUDA PARA ESTILOS ---

    private JPanel crearPanelBase(String titulo) {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_PANEL);
        Border baseBorder = BorderFactory.createLineBorder(COLOR_FONDO, 2, true); // Borde redondeado sutil
        TitledBorder titleBorder = BorderFactory.createTitledBorder(baseBorder, titulo);
        titleBorder.setTitleFont(FUENTE_TITULO);
        titleBorder.setTitleColor(COLOR_TEXTO);
        panel.setBorder(titleBorder);
        return panel;
    }

    private JTextArea crearTextAreaModerna() {
        JTextArea area = new JTextArea();
        area.setBackground(COLOR_PANEL); // Fondo del panel
        area.setForeground(new Color(200, 200, 200)); // Texto claro pero no blanco puro
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        return area;
    }

    private void styleModernButton(JButton btn) {
        btn.setBackground(COLOR_ACCENTO);
        btn.setForeground(Color.BLACK); // Texto oscuro sobre claro
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Espaciado interno
    }

    private void styleModernTabs(JTabbedPane tabs) {
        UIManager.put("TabbedPane.background", COLOR_FONDO);
        UIManager.put("TabbedPane.foreground", COLOR_TEXTO);
        UIManager.put("TabbedPane.selected", COLOR_PANEL);
        UIManager.put("TabbedPane.contentAreaColor", COLOR_PANEL);
        tabs.updateUI(); // Forzar actualización de estilos
    }

    private void styleModernTable(JTable table) {
        table.setBackground(COLOR_PANEL);
        table.setForeground(COLOR_TEXTO);
        table.setGridColor(COLOR_FONDO);
        table.getTableHeader().setBackground(COLOR_PANEL);
        table.getTableHeader().setForeground(COLOR_TEXTO);
        table.getTableHeader().setFont(FUENTE_TITULO);
    }

    // Método principal para arrancar y probar
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}