package interfaz;

import controlador.GestorArchivos;
import modelo.Bloque;
import modelo.Disco;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    // --- Colores Modernos para el Estilo (Paleta "Deep Dark" Profesional) ---
    private final Color COLOR_FONDO = new Color(30, 33, 40);       // Fondo principal más oscuro
    private final Color COLOR_PANEL = new Color(40, 44, 52);       // Paneles un poco más claros
    private final Color COLOR_TEXTO = new Color(220, 224, 232);    // Texto gris claro
    private final Color COLOR_ACCENTO = new Color(65, 131, 215);   // Azul eléctrico moderno
    private final Color COLOR_BOTON = new Color(50, 56, 68);       // Fondo de botones
    
    // Fuentes modernas
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 15);
    private final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 12);

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
    private JButton btnCrearArchivo;
    private JButton btnCrearDirectorio;
    private JButton btnEliminar;
    private JButton btnRenombrar;
    private JButton btnLeer;
    private JButton btnExportarJson;
    private JButton btnImportarJson;
    private JLabel lblCabezaActual;
    private JLabel lblEspacioLibre;

    public VentanaPrincipal() {
        super("Simulador de Sistema de Archivos OS - [MODERNO]");
        
        // 1. INICIALIZAMOS EL CONTROLADOR (El cerebro)
        this.gestor = new GestorArchivos();
        this.gestor.setVentana(this);

        setSize(1350, 900); // Tamaño óptimo para que nada se recorte
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10)); // Espaciado general
        
        // Añadimos un margen general a toda la ventana
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Inicialización Modular de Secciones ---
        add(crearPanelControles(), BorderLayout.NORTH); 
        
        JPanel panelCentralYBottom = new JPanel(new BorderLayout(10, 10));
        panelCentralYBottom.setOpaque(false);
        panelCentralYBottom.add(crearPanelSistemaArchivos(), BorderLayout.WEST); 
        panelCentralYBottom.add(crearPanelTabsCentrales(), BorderLayout.CENTER);   
        panelCentralYBottom.add(crearPanelLogsProcesos(), BorderLayout.SOUTH);   
        
        add(panelCentralYBottom, BorderLayout.CENTER);
    }

    private JPanel crearPanelControles() {
        JPanel panelControles = crearPanelBase("Panel de Control Principal");
        panelControles.setLayout(new BorderLayout(10, 15));

        // Parte superior: Configuración (Admin/Usuario, Planificador, Velocidad)
        JPanel panelConfiguracion = new JPanel(new BorderLayout());
        panelConfiguracion.setOpaque(false);
        panelConfiguracion.add(crearSeccionModoPlanificador(), BorderLayout.WEST);
        panelConfiguracion.add(crearSeccionVelocidadStatus(), BorderLayout.EAST);

        // Parte inferior: Los botones ordenados en Grid
        panelControles.add(panelConfiguracion, BorderLayout.NORTH);
        panelControles.add(crearSeccionBotonesAccion(), BorderLayout.CENTER);
        
        return panelControles;
    }

    private JPanel crearSeccionModoPlanificador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        
        JLabel lblModo = new JLabel("Modo:", JLabel.LEFT);
        lblModo.setForeground(COLOR_TEXTO);
        lblModo.setFont(FUENTE_NORMAL);
        panel.add(lblModo);

        JRadioButton rbAdmin = new JRadioButton("Administrador", true);
        rbAdmin.setForeground(COLOR_TEXTO); rbAdmin.setFont(FUENTE_NORMAL); rbAdmin.setOpaque(false);
        
        JRadioButton rbUsuario = new JRadioButton("Usuario");
        rbUsuario.setForeground(COLOR_TEXTO); rbUsuario.setFont(FUENTE_NORMAL); rbUsuario.setOpaque(false);
        
        ButtonGroup bgModo = new ButtonGroup(); bgModo.add(rbAdmin); bgModo.add(rbUsuario);
        panel.add(rbAdmin); panel.add(rbUsuario);
        
        rbAdmin.addActionListener(e -> actualizarPermisos(true));
        rbUsuario.addActionListener(e -> actualizarPermisos(false));

        JLabel lblPlanificador = new JLabel(" |  Planificador:", JLabel.LEFT);
        lblPlanificador.setForeground(COLOR_TEXTO); lblPlanificador.setFont(FUENTE_NORMAL);
        panel.add(lblPlanificador);
        
        String[] algos = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        JComboBox<String> comboAlgo = new JComboBox<>(algos);
        comboAlgo.setFont(FUENTE_NORMAL);
        comboAlgo.setBackground(COLOR_PANEL);
        comboAlgo.setForeground(Color.WHITE);
        comboAlgo.addActionListener(e -> {
            String seleccion = (String) comboAlgo.getSelectedItem();
            gestor.cambiarPoliticaPlanificador(seleccion);
            areaLog.append("⚙️ Política cambiada a: " + seleccion + "\n");
        });
        panel.add(comboAlgo);
        
        return panel;
    }

    private JPanel crearSeccionBotonesAccion() {
        // Layout Dashboard: 2 filas, 5 columnas. No se recorta ningún texto.
        JPanel panel = new JPanel(new GridLayout(2, 5, 12, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        btnCrearArchivo = new JButton("Crear Archivo");
        styleModernButton(btnCrearArchivo, COLOR_BOTON, COLOR_TEXTO);
        btnCrearArchivo.addActionListener(e -> accionCrearArchivo());
        panel.add(btnCrearArchivo);

        btnCrearDirectorio = new JButton("Crear Directorio");
        styleModernButton(btnCrearDirectorio, COLOR_BOTON, COLOR_TEXTO);
        btnCrearDirectorio.addActionListener(e -> accionCrearDirectorio());
        panel.add(btnCrearDirectorio);

        btnRenombrar = new JButton("Renombrar");
        styleModernButton(btnRenombrar, COLOR_BOTON, COLOR_TEXTO);
        btnRenombrar.addActionListener(e -> accionRenombrar());
        panel.add(btnRenombrar);

        btnEliminar = new JButton("Eliminar");
        styleModernButton(btnEliminar, COLOR_BOTON, COLOR_TEXTO);
        btnEliminar.addActionListener(e -> accionEliminarArchivo());
        panel.add(btnEliminar);

        btnLeer = new JButton("Leer Archivo");
        styleModernButton(btnLeer, COLOR_BOTON, COLOR_TEXTO);
        btnLeer.addActionListener(e -> accionLeerArchivo()); 
        panel.add(btnLeer);

        JButton btnEstadisticas = new JButton("Estadísticas");
        styleModernButton(btnEstadisticas, COLOR_ACCENTO, Color.WHITE);
        btnEstadisticas.addActionListener(e -> accionEstadisticas()); 
        panel.add(btnEstadisticas);
        
        btnImportarJson = new JButton("Importar JSON");
        styleModernButton(btnImportarJson, COLOR_BOTON, COLOR_TEXTO);
        btnImportarJson.addActionListener(e -> accionImportarJSON()); 
        panel.add(btnImportarJson);

        btnExportarJson = new JButton("Exportar JSON");
        styleModernButton(btnExportarJson, COLOR_BOTON, COLOR_TEXTO);
        btnExportarJson.addActionListener(e -> accionExportarJSON()); 
        panel.add(btnExportarJson);
        
        JButton btnFallo = new JButton("Simular Fallo");
        styleModernButton(btnFallo, new Color(217, 83, 79), Color.WHITE); // Rojo
        btnFallo.addActionListener(e -> {
            gestor.simularFallo = true;
            areaLog.append("⚠️ ERROR FORZADO ACTIVADO: El sistema colapsará en la próxima escritura.\n");
        });
        panel.add(btnFallo);
        
        JButton btnRecuperar = new JButton("Reiniciar/Recuperar");
        styleModernButton(btnRecuperar, new Color(92, 184, 92), Color.WHITE); // Verde
        btnRecuperar.addActionListener(e -> {
            gestor.recuperarSistemaDespuesDeFallo();
        });
        panel.add(btnRecuperar);
        
        return panel;
    }

    private JPanel crearSeccionVelocidadStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setOpaque(false);
        
        // --- LO NUEVO: El indicador del cabezal ---
        lblCabezaActual = new JLabel("Cabezal: Bloque 0");
        lblCabezaActual.setForeground(Color.WHITE); 
        lblCabezaActual.setFont(FUENTE_TITULO);
        panel.add(lblCabezaActual);
        // ------------------------------------------

        JLabel lblTituloVelocidad = new JLabel("  |  Velocidad de Disco:", JLabel.RIGHT);
        lblTituloVelocidad.setForeground(COLOR_TEXTO); lblTituloVelocidad.setFont(FUENTE_NORMAL);
        panel.add(lblTituloVelocidad);
        
        JLabel lblVelocidad = new JLabel("5000 ms");
        lblVelocidad.setForeground(COLOR_ACCENTO); lblVelocidad.setFont(FUENTE_BOTON);
        
        sliderVelocidad = new JSlider(100, 10000, 5000);
        sliderVelocidad.setOpaque(false);
        sliderVelocidad.setPreferredSize(new Dimension(150, 20));
        
        sliderVelocidad.addChangeListener(e -> {
            int valorMs = sliderVelocidad.getValue();
            lblVelocidad.setText(valorMs + " ms"); 
            gestor.cambiarVelocidadDisco(valorMs); 
        });

        panel.add(sliderVelocidad);
        panel.add(lblVelocidad);
        
        return panel;
    }

    private JPanel crearPanelSistemaArchivos() {
        JPanel panel = crearPanelBase("Explorador de Archivos");
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setLayout(new BorderLayout());

        String nombreRaiz = gestor.getDirectorioRaiz().getNombre();
        DefaultMutableTreeNode raizNode = new DefaultMutableTreeNode(nombreRaiz);

        arbolDirectorios = new JTree(new DefaultTreeModel(raizNode));
        arbolDirectorios.setBackground(COLOR_PANEL);
        arbolDirectorios.setForeground(COLOR_TEXTO);
        arbolDirectorios.setFont(FUENTE_NORMAL);
        arbolDirectorios.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(arbolDirectorios);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    private JTabbedPane crearPanelTabsCentrales() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FUENTE_NORMAL);
        tabs.setOpaque(false);
        styleModernTabs(tabs);

        tabs.addTab("Simulación de Disco", crearPanelDiscoMap());
        tabs.addTab("Tabla FAT", crearPanelTablaAsignacion());
        tabs.addTab("Rendimiento", crearPanelCostoCache());
        
        return tabs;
    }

    private JPanel crearPanelDiscoMap() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Disco discoReal = gestor.getDisco();
        int totalBloques = discoReal.getCapacidad();
        Bloque[] bloquesReales = discoReal.getBloques();

        int cols = 15; 
        int rows = (int) Math.ceil((double) totalBloques / cols);
        
        panelDiscoBlocks = new JPanel(new GridLayout(rows, cols, 4, 4));
        panelDiscoBlocks.setOpaque(false);
        
        for (int i = 0; i < totalBloques; i++) {
            JPanel block = new JPanel();
            if (bloquesReales[i].isLibre()) {
                block.setBackground(new Color(60, 65, 75));
            } else {
                block.setBackground(COLOR_ACCENTO); 
            }
            
            block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
            JLabel lblId = new JLabel(String.valueOf(bloquesReales[i].getId()));
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblId.setForeground(COLOR_TEXTO);
            block.add(lblId);
            panelDiscoBlocks.add(block);
        }
        
        JScrollPane scrollDisco = new JScrollPane(panelDiscoBlocks);
        scrollDisco.setOpaque(false);
        scrollDisco.getViewport().setOpaque(false);
        scrollDisco.setBorder(null);
        panel.add(scrollDisco, BorderLayout.CENTER);

        // --- AQUÍ ESTÁ LA CORRECCIÓN LIMPIA ---
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelStatus.setOpaque(false);
        
        // Usamos la variable global lblEspacioLibre que debiste declarar al inicio de la clase
        lblEspacioLibre = new JLabel("Espacio Libre: " + discoReal.obtenerEspacioLibre() + " / " + totalBloques + " bloques");
        lblEspacioLibre.setFont(FUENTE_TITULO);
        lblEspacioLibre.setForeground(COLOR_ACCENTO);
        
        panelStatus.add(lblEspacioLibre);
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
        tablaArchivos.setFont(FUENTE_NORMAL);
        tablaArchivos.setRowHeight(25);
        styleModernTable(tablaArchivos);

        JScrollPane scroll = new JScrollPane(tablaArchivos);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelCostoCache() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Métricas de Rendimiento Próximamente...", JLabel.CENTER);
        lbl.setForeground(COLOR_TEXTO); lbl.setFont(FUENTE_TITULO);
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelLogsProcesos() {
        JPanel panelLogsProcesos = new JPanel(new GridLayout(1, 2, 10, 0));
        panelLogsProcesos.setOpaque(false);
        panelLogsProcesos.setPreferredSize(new Dimension(0, 260));

        JPanel panelLog = crearPanelBase("Log de Eventos (Journaling)");
        panelLog.setLayout(new BorderLayout());
        areaLog = crearTextAreaModerna();
        areaLog.append("✅ Sistema iniciado correctamente...\n");
        panelLog.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        
        JButton btnLimpiarLog = new JButton("Limpiar Log");
        styleModernButton(btnLimpiarLog, COLOR_BOTON, COLOR_TEXTO);
        btnLimpiarLog.addActionListener(e -> areaLog.setText("✅ Sistema iniciado correctamente...\n"));
        JPanel panelSurLog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSurLog.setOpaque(false); panelSurLog.add(btnLimpiarLog);
        panelLog.add(panelSurLog, BorderLayout.SOUTH);
        
        panelLogsProcesos.add(panelLog);

        JPanel panelProcesos = crearPanelBase("Telemetría de Procesos");
        panelProcesos.setLayout(new BorderLayout());
        areaProcesos = crearTextAreaModerna();
        areaProcesos.append("=== LISTOS ===\n\n=== EN CPU ===\n\n=== BLOQUEADOS ===\n\n=== COLA I/O ===\n");
        panelProcesos.add(new JScrollPane(areaProcesos), BorderLayout.CENTER);
        panelLogsProcesos.add(panelProcesos);

        return panelLogsProcesos;
    }

    // --- MÉTODOS DE ESTILIZADO PROFESIONAL ---

    private JPanel crearPanelBase(String titulo) {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_PANEL);
        Border borderBase = BorderFactory.createLineBorder(new Color(60, 65, 75), 1, true);
        TitledBorder titleBorder = BorderFactory.createTitledBorder(borderBase, "  " + titulo + "  ");
        titleBorder.setTitleFont(FUENTE_TITULO);
        titleBorder.setTitleColor(COLOR_ACCENTO);
        panel.setBorder(new CompoundBorder(titleBorder, new EmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    private JTextArea crearTextAreaModerna() {
        JTextArea area = new JTextArea();
        area.setBackground(new Color(25, 28, 35)); 
        area.setForeground(new Color(160, 220, 160)); 
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private void styleModernButton(JButton btn, Color bgColor, Color fgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFont(FUENTE_BOTON);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FONDO, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
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
        table.setGridColor(new Color(60, 65, 75));
        table.getTableHeader().setBackground(new Color(25, 28, 35));
        table.getTableHeader().setForeground(COLOR_ACCENTO);
        table.getTableHeader().setFont(FUENTE_TITULO);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35)); 
    }

    // --- MÉTODOS DE ACTUALIZACIÓN DEL SISTEMA ---
    
    public void agregarMensajeLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public void actualizarPantallaProcesos(String texto) {
        SwingUtilities.invokeLater(() -> areaProcesos.setText(texto));
    }

    public void actualizarPantallaCompleta() {
        
        SwingUtilities.invokeLater(() -> {
        // --- 1. Refrescar Mapa de Disco ---
        panelDiscoBlocks.removeAll();
        modelo.Bloque[] bloquesReales = gestor.getDisco().getBloques();
        
        for (int i = 0; i < gestor.getDisco().getCapacidad(); i++) {
            JPanel block = new JPanel();
            block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
            
            if (bloquesReales[i].isLibre()) {
                block.setBackground(new Color(60, 65, 75)); // Gris
                block.setToolTipText("Bloque " + i + ": Libre"); 
            } else {
                String nombreArch = bloquesReales[i].getArchivoAsignado();
                int hash = Math.abs(nombreArch.hashCode());
                int r = (hash & 0xFF0000) >> 16;
                int g = (hash & 0x00FF00) >> 8;
                int b = hash & 0x0000FF;
                Color colorArchivo = new Color(r % 150 + 100, g % 150 + 100, b % 150 + 100);
                
                block.setBackground(colorArchivo);
                block.setToolTipText("Bloque " + i + ": Ocupado por '" + nombreArch + "'"); 
            }
            
            JLabel lblId = new JLabel(String.valueOf(i));
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblId.setForeground(Color.WHITE);
            block.add(lblId);
            panelDiscoBlocks.add(block);
        }
        panelDiscoBlocks.revalidate();
        panelDiscoBlocks.repaint();
        if (lblEspacioLibre != null) {
            lblEspacioLibre.setText("Espacio Libre: " + gestor.getDisco().obtenerEspacioLibre() + " / " + gestor.getDisco().getCapacidad() + " bloques");
        }

        // --- 2. Refrescar Tabla de Asignación ---
        modeloTabla.setRowCount(0); 
        // Llamamos al nuevo método recursivo para que lea TODAS las carpetas
        llenarTablaRecursiva(gestor.getDirectorioRaiz());

        // --- 3. Refrescar Árbol de Directorios (JTree) ---
        DefaultMutableTreeNode raizNode = (DefaultMutableTreeNode) arbolDirectorios.getModel().getRoot();
        raizNode.removeAllChildren(); 
        
        // Usamos el método recursivo desde la raíz
        construirArbolRecursivo(gestor.getDirectorioRaiz(), raizNode);
        
        ((DefaultTreeModel) arbolDirectorios.getModel()).reload();
        
        // Expandimos todas las carpetas automáticamente para que se vea bien
        for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
            arbolDirectorios.expandRow(i);
        }
        
        });
    }

    // --- LÓGICA DE EVENTOS (ACCIONES) ---
    
    private void accionCrearArchivo() {
        // 1. Pedir nombre del archivo
        String nombre = JOptionPane.showInputDialog(this, 
                "Ingrese el nombre del nuevo archivo (Ej: reporte.txt):", 
                "Crear Archivo", 
                JOptionPane.QUESTION_MESSAGE);
        
        // Validación 1: Que el usuario no cancele ni deje el nombre vacío
        if (nombre == null || nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "El nombre del archivo no puede estar vacío.", 
                    "Error de Validación", 
                    JOptionPane.ERROR_MESSAGE);
            return; // Detenemos la ejecución aquí sin romper el programa
        }
        
        // 2. Pedir tamaño del archivo
        String tamanoStr = JOptionPane.showInputDialog(this, 
                "Ingrese el tamaño en bloques (número entero positivo):", 
                "Tamaño del Archivo", 
                JOptionPane.QUESTION_MESSAGE);
        
        // Si el usuario le da a "Cancelar", simplemente salimos
        if (tamanoStr == null || tamanoStr.trim().isEmpty()) {
            return; 
        }
        
        try {
            // Validación 2: Tipo de dato (Verificamos que sea un número entero)
            int tamano = Integer.parseInt(tamanoStr.trim());
            
            // Validación 3: Rango (No puede ser 0 ni negativo)
            if (tamano <= 0) {
                JOptionPane.showMessageDialog(this, 
                        "El tamaño debe ser mayor a 0 bloques.", 
                        "Error de Rango", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validación 4: Espacio disponible (¡Esto da muchos puntos!)
            if (tamano > gestor.getDisco().obtenerEspacioLibre()) {
                JOptionPane.showMessageDialog(this, 
                        "No hay suficiente espacio libre en el disco para " + tamano + " bloques.", 
                        "Error de Espacio", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Crear el archivo en el sistema
            // (Asumimos que "admin" es el usuario por defecto, cámbialo si manejas sesiones)
            boolean exito = gestor.crearArchivo(nombre.trim(), tamano, "admin"); 
            
            if (exito) {
                // Notificamos el éxito por consola visual si la tienes, o por popup
                JOptionPane.showMessageDialog(this, 
                        "Archivo '" + nombre + "' creado exitosamente.", 
                        "Éxito", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                // ¡AQUÍ LLAMAMOS AL MÉTODO QUE ARREGLAMOS ANTES!
                actualizarPantallaCompleta(); 
            } else {
                JOptionPane.showMessageDialog(this, 
                        "No se pudo crear el archivo. Revisa si ya existe uno con ese nombre.", 
                        "Error de Creación", 
                        JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            // Validación 5: Manejo del error si el usuario escribe letras (Ej: "cinco")
            JOptionPane.showMessageDialog(this, 
                    "Debe ingresar un número entero válido (Ej: 5). No se admiten letras ni decimales.", 
                    "Error de Tipo de Dato", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminarArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo o carpeta a eliminar:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        // Mandamos a la cola de I/O
        String mensaje = gestor.encolarSolicitudEliminacion(nombre);
        
        JOptionPane.showMessageDialog(this, mensaje, "Solicitud Encolada", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void accionCrearDirectorio() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        gestor.crearDirectorio(nombre);
        areaLog.append("📁 Directorio creado: " + nombre + "\n");
        actualizarPantallaCompleta();
    }

    private void accionRenombrar() {
        String nombreAntiguo = JOptionPane.showInputDialog(this, "Ingrese el nombre ACTUAL del archivo o carpeta:");
        if (nombreAntiguo == null || nombreAntiguo.trim().isEmpty()) return;

        String nombreNuevo = JOptionPane.showInputDialog(this, "Ingrese el NUEVO nombre:");
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) return;

        boolean exito = gestor.renombrarItem(nombreAntiguo, nombreNuevo);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Renombrado con éxito a: " + nombreNuevo, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            areaLog.append("✏️ Renombrado: '" + nombreAntiguo + "' -> '" + nombreNuevo + "'\n");
            actualizarPantallaCompleta(); 
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró nada con el nombre '" + nombreAntiguo + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void accionEstadisticas() {
        String reporte = gestor.obtenerEstadisticas();
        JOptionPane.showMessageDialog(this, reporte, "Estadísticas del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void accionLeerArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo a leer:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String resultado = gestor.leerArchivo(nombre);

        if (resultado != null) {
            JOptionPane.showMessageDialog(this, resultado, "Lectura de Disco", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo '" + nombre + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPermisos(boolean esAdmin) {
        btnCrearArchivo.setEnabled(esAdmin);
        btnCrearDirectorio.setEnabled(esAdmin);
        btnEliminar.setEnabled(esAdmin);
        btnRenombrar.setEnabled(esAdmin);
    
        if(!esAdmin) {
            areaLog.append("⚠️ Modo Usuario: Acceso restringido a solo lectura.\n");
        } else {
            areaLog.append("🔓 Modo Administrador: Acceso total habilitado.\n");
        }
    }

    private void accionExportarJSON() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar estado del disco como JSON");
        
        int seleccion = fileChooser.showSaveDialog(this);
        
        if (seleccion == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoDestino = fileChooser.getSelectedFile();
            String ruta = archivoDestino.getAbsolutePath();
            
            if (!ruta.toLowerCase().endsWith(".json")) {
                ruta += ".json";
            }
            
            String mensaje = gestor.exportarAJson(ruta);
            javax.swing.JOptionPane.showMessageDialog(this, mensaje, "Exportación", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void accionImportarJSON() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Cargar estado del disco desde JSON");
        
        int seleccion = fileChooser.showOpenDialog(this);
        
        if (seleccion == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoOrigen = fileChooser.getSelectedFile();
            String ruta = archivoOrigen.getAbsolutePath();
            
            String mensaje = gestor.importarDeJson(ruta);
            javax.swing.JOptionPane.showMessageDialog(this, mensaje, "Importación", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            
            actualizarPantallaCompleta();
            areaLog.append("📂 JSON cargado desde: " + archivoOrigen.getName() + "\n");
        }
    }
    
    public void actualizarPosicionCabezalEnVivo(int posicion) {
        SwingUtilities.invokeLater(() -> {
            if (lblCabezaActual != null) {
                lblCabezaActual.setText("Cabezal: Bloque " + posicion);
            }
        });
    }
    
    // Método recursivo para construir el árbol sin importar cuántas carpetas haya adentro
    private void construirArbolRecursivo(modelo.Directorio dirActual, DefaultMutableTreeNode nodoActual) {
        
        // 1. Primero agregamos las subcarpetas de este nivel
        estructuras.ListaEnlazada<modelo.Directorio> subdirs = dirActual.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            modelo.Directorio subDir = subdirs.obtener(i);
            DefaultMutableTreeNode nodoSubDir = new DefaultMutableTreeNode("📁 " + subDir.getNombre());
            nodoActual.add(nodoSubDir);
            
            // ¡La Magia! Nos llamamos a nosotros mismos para buscar qué hay dentro de esta subcarpeta
            construirArbolRecursivo(subDir, nodoSubDir); 
        }

        // 2. Luego agregamos los archivos sueltos de este nivel
        estructuras.ListaEnlazada<modelo.Archivo> archivos = dirActual.getArchivos();
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            nodoActual.add(new DefaultMutableTreeNode("📄 " + arch.getNombre() + " [" + arch.getTamañoEnBloques() + " blk]")); 
        }
    }
    
    // Método recursivo para que la Tabla FAT encuentre los archivos en TODAS las subcarpetas
    private void llenarTablaRecursiva(modelo.Directorio dirActual) {
        estructuras.ListaEnlazada<modelo.Archivo> archivos = dirActual.getArchivos();
        
        for (int i = 0; i < archivos.getTamano(); i++) {
            modelo.Archivo arch = archivos.obtener(i);
            
            String estadoLock = "Libre";
            if (arch.isSiendoEscrito()) estadoLock = "🔒 Escribiendo (Exclusivo)";
            else if (arch.getLectoresActivos() > 0) estadoLock = "👁️ Leyendo (" + arch.getLectoresActivos() + ")";

            modeloTabla.addRow(new Object[]{
                arch.getNombre(), arch.getPropietario(), arch.getTamañoEnBloques(), arch.getBloqueInicial(), estadoLock
            });
        }
        
        // Buscar en las subcarpetas
        estructuras.ListaEnlazada<modelo.Directorio> subdirs = dirActual.getSubdirectorios();
        for (int i = 0; i < subdirs.getTamano(); i++) {
            llenarTablaRecursiva(subdirs.obtener(i));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}