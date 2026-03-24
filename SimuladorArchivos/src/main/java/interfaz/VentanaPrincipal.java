package interfaz;

import controlador.GestorArchivos;
import modelo.Archivo;
import modelo.Bloque;
import modelo.Disco;
import modelo.Directorio;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final Color COLOR_FONDO = new Color(30, 33, 40);
    private final Color COLOR_PANEL = new Color(40, 44, 52);
    private final Color COLOR_TEXTO = new Color(220, 224, 232);
    private final Color COLOR_ACCENTO = new Color(65, 131, 215);
    private final Color COLOR_BOTON = new Color(50, 56, 68);

    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 15);
    private final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 12);

    private GestorArchivos gestor;

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
    private JLabel lblDetalleNombre;
    private JLabel lblDetalleTipo;
    private JLabel lblDetalleTamano;
    private JLabel lblDetalleDueno;
    private JTextField txtCabezalInicial;

    private boolean modoAdminActual = true;
    private String usuarioActual = "admin";

    public VentanaPrincipal() {
        super("Simulador de Sistema de Archivos OS");

        this.gestor = new GestorArchivos();
        this.gestor.setVentana(this);

        setSize(1350, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(crearPanelControles(), BorderLayout.NORTH);

        JPanel panelCentralYBottom = new JPanel(new BorderLayout(10, 10));
        panelCentralYBottom.setOpaque(false);
        panelCentralYBottom.add(crearPanelSistemaArchivos(), BorderLayout.WEST);
        panelCentralYBottom.add(crearPanelTabsCentrales(), BorderLayout.CENTER);
        panelCentralYBottom.add(crearPanelLogsProcesos(), BorderLayout.SOUTH);

        add(panelCentralYBottom, BorderLayout.CENTER);

        actualizarPantallaCompleta();
        actualizarPermisos(true);
    }

    private JPanel crearPanelControles() {
        JPanel panelControles = crearPanelBase("Panel de Control Principal");
        panelControles.setLayout(new BorderLayout(10, 15));

        JPanel panelConfiguracion = new JPanel(new BorderLayout());
        panelConfiguracion.setOpaque(false);
        panelConfiguracion.add(crearSeccionModoPlanificador(), BorderLayout.WEST);
        panelConfiguracion.add(crearSeccionVelocidadStatus(), BorderLayout.EAST);

        panelControles.add(panelConfiguracion, BorderLayout.NORTH);
        panelControles.add(crearSeccionBotonesAccion(), BorderLayout.CENTER);

        return panelControles;
    }

    private JPanel crearSeccionModoPlanificador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);

        JLabel lblModo = new JLabel("Modo:");
        lblModo.setForeground(COLOR_TEXTO);
        lblModo.setFont(FUENTE_NORMAL);
        panel.add(lblModo);

        JRadioButton rbAdmin = new JRadioButton("Administrador", true);
        rbAdmin.setForeground(COLOR_TEXTO);
        rbAdmin.setFont(FUENTE_NORMAL);
        rbAdmin.setOpaque(false);

        JRadioButton rbUsuario = new JRadioButton("Usuario");
        rbUsuario.setForeground(COLOR_TEXTO);
        rbUsuario.setFont(FUENTE_NORMAL);
        rbUsuario.setOpaque(false);

        ButtonGroup bgModo = new ButtonGroup();
        bgModo.add(rbAdmin);
        bgModo.add(rbUsuario);
        panel.add(rbAdmin);
        panel.add(rbUsuario);

        rbAdmin.addActionListener(e -> actualizarPermisos(true));
        rbUsuario.addActionListener(e -> actualizarPermisos(false));

        JLabel lblPlanificador = new JLabel(" |  Planificador:");
        lblPlanificador.setForeground(COLOR_TEXTO);
        lblPlanificador.setFont(FUENTE_NORMAL);
        panel.add(lblPlanificador);

        String[] algos = {"FIFO", "SSTF", "SCAN", "C-SCAN"};
        JComboBox<String> comboAlgo = new JComboBox<>(algos);
        comboAlgo.setFont(FUENTE_NORMAL);
        comboAlgo.setBackground(COLOR_PANEL);
        comboAlgo.setForeground(Color.WHITE);
        comboAlgo.addActionListener(e -> {
            String seleccion = (String) comboAlgo.getSelectedItem();
            gestor.cambiarPoliticaPlanificador(seleccion);
            agregarMensajeLog("⚙️ Política cambiada a: " + seleccion);
        });
        panel.add(comboAlgo);

        return panel;
    }

    private JPanel crearSeccionBotonesAccion() {
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
        styleModernButton(btnFallo, new Color(217, 83, 79), Color.WHITE);
        btnFallo.addActionListener(e -> {
            gestor.simularFallo = true;
            agregarMensajeLog("⚠️ ERROR FORZADO ACTIVADO: El sistema colapsará en la próxima escritura.");
        });
        panel.add(btnFallo);

        JButton btnRecuperar = new JButton("Reiniciar/Recuperar");
        styleModernButton(btnRecuperar, new Color(92, 184, 92), Color.WHITE);
        btnRecuperar.addActionListener(e -> gestor.recuperarSistemaDespuesDeFallo());
        panel.add(btnRecuperar);

        return panel;
    }

    private JPanel crearSeccionVelocidadStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setOpaque(false);

        lblCabezaActual = new JLabel("Cabezal: Bloque 0");
        lblCabezaActual.setForeground(Color.WHITE);
        lblCabezaActual.setFont(FUENTE_TITULO);
        panel.add(lblCabezaActual);

        JLabel lblTituloCabezal = new JLabel(" | Cabezal inicial:");
        lblTituloCabezal.setForeground(COLOR_TEXTO);
        lblTituloCabezal.setFont(FUENTE_NORMAL);
        panel.add(lblTituloCabezal);

        txtCabezalInicial = new JTextField("0", 4);
        txtCabezalInicial.setBackground(COLOR_PANEL);
        txtCabezalInicial.setForeground(Color.WHITE);
        txtCabezalInicial.setCaretColor(Color.WHITE);
        txtCabezalInicial.setFont(FUENTE_NORMAL);
        panel.add(txtCabezalInicial);

        JButton btnFijarCabezal = new JButton("Fijar");
        styleModernButton(btnFijarCabezal, COLOR_BOTON, COLOR_TEXTO);
        btnFijarCabezal.addActionListener(e -> fijarCabezalInicial());
        panel.add(btnFijarCabezal);

        JLabel lblTituloVelocidad = new JLabel(" | Velocidad:");
        lblTituloVelocidad.setForeground(COLOR_TEXTO);
        lblTituloVelocidad.setFont(FUENTE_NORMAL);
        panel.add(lblTituloVelocidad);

        JLabel lblVelocidad = new JLabel("500 ms");
        lblVelocidad.setForeground(COLOR_ACCENTO);
        lblVelocidad.setFont(FUENTE_BOTON);

        sliderVelocidad = new JSlider(100, 3000, 500);
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
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BorderLayout(0, 10));

        DefaultMutableTreeNode raizNode = new DefaultMutableTreeNode("📁 " + gestor.getDirectorioRaiz().getNombre());
        arbolDirectorios = new JTree(new DefaultTreeModel(raizNode));
        arbolDirectorios.setBackground(COLOR_PANEL);
        arbolDirectorios.setForeground(COLOR_TEXTO);
        arbolDirectorios.setFont(FUENTE_NORMAL);
        arbolDirectorios.setBorder(new EmptyBorder(10, 10, 10, 10));
        arbolDirectorios.addTreeSelectionListener(this::actualizarDetallesDesdeSeleccion);

        JScrollPane scroll = new JScrollPane(arbolDirectorios);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(crearPanelDetallesSeleccion(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelDetallesSeleccion() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 4));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 65, 75), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        lblDetalleNombre = crearLabelDetalle("Nombre: -");
        lblDetalleTipo = crearLabelDetalle("Tipo: -");
        lblDetalleTamano = crearLabelDetalle("Tamaño: -");
        lblDetalleDueno = crearLabelDetalle("Dueño: -");

        panel.add(lblDetalleNombre);
        panel.add(lblDetalleTipo);
        panel.add(lblDetalleTamano);
        panel.add(lblDetalleDueno);
        return panel;
    }

    private JLabel crearLabelDetalle(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_TEXTO);
        lbl.setFont(FUENTE_NORMAL);
        return lbl;
    }

    private JTabbedPane crearPanelTabsCentrales() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FUENTE_NORMAL);
        tabs.setOpaque(false);
        styleModernTabs(tabs);

        tabs.addTab("Simulación de Disco", crearPanelDiscoMap());
        tabs.addTab("Tabla FAT", crearPanelTablaAsignacion());

        return tabs;
    }

    private JPanel crearPanelDiscoMap() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Disco discoReal = gestor.getDisco();
        int totalBloques = discoReal.getCapacidad();
        int cols = 15;
        int rows = (int) Math.ceil((double) totalBloques / cols);

        panelDiscoBlocks = new JPanel(new GridLayout(rows, cols, 4, 4));
        panelDiscoBlocks.setOpaque(false);

        for (int i = 0; i < totalBloques; i++) {
            JPanel block = crearBloqueVisual(i, discoReal.getBloques()[i]);
            panelDiscoBlocks.add(block);
        }

        JScrollPane scrollDisco = new JScrollPane(panelDiscoBlocks);
        scrollDisco.setOpaque(false);
        scrollDisco.getViewport().setOpaque(false);
        scrollDisco.setBorder(null);
        panel.add(scrollDisco, BorderLayout.CENTER);

        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelStatus.setOpaque(false);

        lblEspacioLibre = new JLabel("Espacio Libre: " + discoReal.obtenerEspacioLibre() + " / " + totalBloques + " bloques");
        lblEspacioLibre.setFont(FUENTE_TITULO);
        lblEspacioLibre.setForeground(COLOR_ACCENTO);

        panelStatus.add(lblEspacioLibre);
        panel.add(panelStatus, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearBloqueVisual(int indice, Bloque bloque) {
        JPanel block = new JPanel();
        block.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));

        if (bloque.isLibre()) {
            block.setBackground(new Color(60, 65, 75));
            block.setToolTipText("Bloque " + indice + ": Libre");
        } else {
            String nombreArch = bloque.getArchivoAsignado();
            block.setBackground(colorPorNombre(nombreArch));
            block.setToolTipText("Bloque " + indice + ": Ocupado por '" + nombreArch + "'");
        }

        JLabel lblId = new JLabel(String.valueOf(indice));
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblId.setForeground(Color.WHITE);
        block.add(lblId);
        return block;
    }

    private JPanel crearPanelTablaAsignacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Nombre", "Dueño", "Tamaño", "Bloque Inicial", "Lock", "Color"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaArchivos = new JTable(modeloTabla);
        tablaArchivos.setFont(FUENTE_NORMAL);
        tablaArchivos.setRowHeight(25);
        styleModernTable(tablaArchivos);

        JScrollPane scroll = new JScrollPane(tablaArchivos);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_FONDO, 1));
        panel.add(scroll, BorderLayout.CENTER);
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
        panelSurLog.setOpaque(false);
        panelSurLog.add(btnLimpiarLog);
        panelLog.add(panelSurLog, BorderLayout.SOUTH);

        panelLogsProcesos.add(panelLog);

        JPanel panelProcesos = crearPanelBase("Telemetría de Procesos");
        panelProcesos.setLayout(new BorderLayout());
        areaProcesos = crearTextAreaModerna();
        areaProcesos.append("=== COLA DE PROCESOS (I/O) ===\n");
        panelProcesos.add(new JScrollPane(areaProcesos), BorderLayout.CENTER);
        panelLogsProcesos.add(panelProcesos);

        return panelLogsProcesos;
    }

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

    public void agregarMensajeLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public void escribirLog(String mensaje) {
        agregarMensajeLog("» " + mensaje);
    }

    public void actualizarPantallaProcesos(String texto) {
        SwingUtilities.invokeLater(() -> areaProcesos.setText(texto));
    }

    public void actualizarPantallaCompleta() {
        SwingUtilities.invokeLater(() -> {
            if (panelDiscoBlocks != null) {
                panelDiscoBlocks.removeAll();
                Bloque[] bloquesReales = gestor.getDisco().getBloques();
                for (int i = 0; i < gestor.getDisco().getCapacidad(); i++) {
                    panelDiscoBlocks.add(crearBloqueVisual(i, bloquesReales[i]));
                }
                panelDiscoBlocks.revalidate();
                panelDiscoBlocks.repaint();
            }

            if (lblEspacioLibre != null) {
                lblEspacioLibre.setText("Espacio Libre: " + gestor.getDisco().obtenerEspacioLibre()
                        + " / " + gestor.getDisco().getCapacidad() + " bloques");
            }

            if (modeloTabla != null) {
                modeloTabla.setRowCount(0);
                llenarTablaRecursiva(gestor.getDirectorioRaiz());
            }

            if (arbolDirectorios != null) {
                DefaultMutableTreeNode raizNode = new DefaultMutableTreeNode("📁 " + gestor.getDirectorioRaiz().getNombre());
                construirArbolRecursivo(gestor.getDirectorioRaiz(), raizNode);
                arbolDirectorios.setModel(new DefaultTreeModel(raizNode));
                for (int i = 0; i < arbolDirectorios.getRowCount(); i++) {
                    arbolDirectorios.expandRow(i);
                }
            }

            actualizarPosicionCabezalEnVivo(gestor.getPosicionCabeza());
            limpiarDetalles();
        });
    }

    private void accionCrearArchivo() {
        if (!modoAdminActual) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede crear archivos.", "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = JOptionPane.showInputDialog(this,
                "Ingrese el nombre del nuevo archivo (Ej: reporte.txt):",
                "Crear Archivo",
                JOptionPane.QUESTION_MESSAGE);

        if (nombre == null || nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del archivo no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tamanoStr = JOptionPane.showInputDialog(this,
                "Ingrese el tamaño en bloques (número entero positivo):",
                "Tamaño del Archivo",
                JOptionPane.QUESTION_MESSAGE);

        if (tamanoStr == null || tamanoStr.trim().isEmpty()) {
            return;
        }

        try {
            int tamano = Integer.parseInt(tamanoStr.trim());
            if (tamano <= 0) {
                JOptionPane.showMessageDialog(this, "El tamaño debe ser mayor a 0 bloques.", "Error de Rango", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tamano > gestor.getDisco().obtenerEspacioLibre()) {
                JOptionPane.showMessageDialog(this,
                        "No hay suficiente espacio libre en el disco para " + tamano + " bloques.",
                        "Error de Espacio",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String directorioDestino = obtenerDirectorioSeleccionado();
            String mensaje = gestor.encolarSolicitudCreacion(nombre.trim(), tamano, usuarioActual, directorioDestino);
            JOptionPane.showMessageDialog(this,
                    mensaje + "\nDestino: " + directorioDestino,
                    "Solicitud Encolada",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un número entero válido (Ej: 5).",
                    "Error de Tipo de Dato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminarArchivo() {
        if (!modoAdminActual) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede eliminar archivos o carpetas.", "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo o carpeta a eliminar:");
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }

        String mensaje = gestor.encolarSolicitudEliminacion(nombre.trim());
        JOptionPane.showMessageDialog(this, mensaje, "Solicitud Encolada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionCrearDirectorio() {
        if (!modoAdminActual) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede crear directorios.", "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre de la nueva carpeta:");
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }

        String directorioDestino = obtenerDirectorioSeleccionado();
        boolean exito = gestor.crearDirectorio(nombre.trim(), usuarioActual, directorioDestino);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Directorio creado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            agregarMensajeLog("📁 Directorio creado: " + nombre.trim() + " @ " + directorioDestino);
            actualizarPantallaCompleta();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el directorio. Puede que ya exista uno con ese nombre.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionRenombrar() {
        if (!modoAdminActual) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede renombrar archivos o carpetas.", "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreAntiguo = JOptionPane.showInputDialog(this, "Ingrese el nombre ACTUAL del archivo o carpeta:");
        if (nombreAntiguo == null || nombreAntiguo.trim().isEmpty()) {
            return;
        }

        String nombreNuevo = JOptionPane.showInputDialog(this, "Ingrese el NUEVO nombre:");
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty()) {
            return;
        }

        String mensaje = gestor.encolarSolicitudActualizacion(nombreAntiguo.trim(), nombreNuevo.trim());

        if (mensaje.startsWith("⏳")) {
            JOptionPane.showMessageDialog(this, mensaje, "Solicitud Encolada", JOptionPane.INFORMATION_MESSAGE);
            agregarMensajeLog("✏️ UPDATE encolado: '" + nombreAntiguo.trim() + "' -> '" + nombreNuevo.trim() + "'");
        } else {
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEstadisticas() {
        String reporte = gestor.obtenerEstadisticas();
        JOptionPane.showMessageDialog(this, reporte, "Estadísticas del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionLeerArchivo() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo a leer:");
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }

        String resultado = gestor.leerArchivo(nombre.trim());
        if (resultado != null) {
            JOptionPane.showMessageDialog(this, resultado, "Lectura de Disco", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo '" + nombre.trim() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerNombreNodoSeleccionado() {
        if (arbolDirectorios == null || arbolDirectorios.getSelectionPath() == null) {
            return "raiz";
        }

        Object seleccionado = arbolDirectorios.getSelectionPath().getLastPathComponent();
        if (!(seleccionado instanceof DefaultMutableTreeNode)) {
            return "raiz";
        }

        String textoNodo = ((DefaultMutableTreeNode) seleccionado).getUserObject().toString();
        return limpiarTextoNodo(textoNodo);
    }

    private String obtenerDirectorioSeleccionado() {
        String nombre = obtenerNombreNodoSeleccionado();
        Archivo arch = gestor.buscarArchivoObj(nombre);
        if (arch != null) {
            return gestor.nombreDirectorioDeArchivo(nombre);
        }

        Directorio dir = gestor.buscarDirectorioObj(nombre);
        if (dir != null) {
            return dir.getNombre();
        }

        return "raiz";
    }

    private void actualizarPermisos(boolean esAdmin) {
        this.modoAdminActual = esAdmin;
        this.usuarioActual = esAdmin ? "admin" : "usuario1";

        btnCrearArchivo.setEnabled(esAdmin);
        btnCrearDirectorio.setEnabled(esAdmin);
        btnEliminar.setEnabled(esAdmin);
        btnRenombrar.setEnabled(esAdmin);
        btnLeer.setEnabled(true);
        btnExportarJson.setEnabled(true);
        btnImportarJson.setEnabled(true);

        if (!esAdmin) {
            agregarMensajeLog("⚠️ Modo Usuario: Acceso restringido a solo lectura.");
        } else {
            agregarMensajeLog("🔓 Modo Administrador: Acceso total habilitado.");
        }
    }

    private void accionExportarJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar estado del disco como JSON");

        int seleccion = fileChooser.showSaveDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            java.io.File archivoDestino = fileChooser.getSelectedFile();
            String ruta = archivoDestino.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".json")) {
                ruta += ".json";
            }
            String mensaje = gestor.exportarAJson(ruta);
            JOptionPane.showMessageDialog(this, mensaje, "Exportación", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void accionImportarJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Cargar estado del disco desde JSON");

        int seleccion = fileChooser.showOpenDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            java.io.File archivoOrigen = fileChooser.getSelectedFile();
            String ruta = archivoOrigen.getAbsolutePath();

            String mensaje = gestor.importarDeJson(ruta);
            JOptionPane.showMessageDialog(this, mensaje, "Importación", JOptionPane.INFORMATION_MESSAGE);
            actualizarPantallaCompleta();
            agregarMensajeLog("📂 JSON cargado desde: " + archivoOrigen.getName());
        }
    }

    private void fijarCabezalInicial() {
        try {
            int pos = Integer.parseInt(txtCabezalInicial.getText().trim());
            if (pos < 0 || pos >= gestor.getDisco().getCapacidad()) {
                JOptionPane.showMessageDialog(this, "Posición fuera de rango.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            gestor.setPosicionCabeza(pos);
            actualizarPosicionCabezalEnVivo(pos);
            agregarMensajeLog("📍 Cabezal inicial fijado en bloque " + pos);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido para el cabezal.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarPosicionCabezalEnVivo(int posicion) {
        SwingUtilities.invokeLater(() -> {
            if (lblCabezaActual != null) {
                lblCabezaActual.setText("Cabezal: Bloque " + posicion);
            }
        });
    }

    private void construirArbolRecursivo(Directorio dirActual, DefaultMutableTreeNode nodoActual) {
        if (dirActual == null || nodoActual == null) {
            return;
        }

        for (int i = 0; i < dirActual.getSubdirectorios().getTamano(); i++) {
            Directorio subDir = dirActual.getSubdirectorios().obtener(i);
            DefaultMutableTreeNode nodoSubDir = new DefaultMutableTreeNode("📁 " + subDir.getNombre());
            nodoActual.add(nodoSubDir);
            construirArbolRecursivo(subDir, nodoSubDir);
        }

        for (int i = 0; i < dirActual.getArchivos().getTamano(); i++) {
            Archivo arch = dirActual.getArchivos().obtener(i);
            nodoActual.add(new DefaultMutableTreeNode("📄 " + arch.getNombre() + " [" + arch.getTamañoEnBloques() + " blk]"));
        }
    }

    private void llenarTablaRecursiva(Directorio dirActual) {
        if (dirActual == null) {
            return;
        }

        for (int i = 0; i < dirActual.getArchivos().getTamano(); i++) {
            Archivo arch = dirActual.getArchivos().obtener(i);

            String estadoLock = arch.getResumenLock();

            String colorHex = colorHexPorNombre(arch.getNombre());

            modeloTabla.addRow(new Object[]{
                    arch.getNombre(),
                    arch.getPropietario(),
                    arch.getTamañoEnBloques(),
                    arch.getBloqueInicial(),
                    estadoLock,
                    colorHex
            });
        }

        for (int i = 0; i < dirActual.getSubdirectorios().getTamano(); i++) {
            llenarTablaRecursiva(dirActual.getSubdirectorios().obtener(i));
        }
    }

    private void actualizarDetallesDesdeSeleccion(TreeSelectionEvent event) {
        Object seleccionado = event.getPath().getLastPathComponent();
        if (!(seleccionado instanceof DefaultMutableTreeNode)) {
            limpiarDetalles();
            return;
        }

        String textoNodo = ((DefaultMutableTreeNode) seleccionado).getUserObject().toString();
        String nombreLimpio = limpiarTextoNodo(textoNodo);

        if (textoNodo.startsWith("📄")) {
            Archivo arch = gestor.buscarArchivoObj(nombreLimpio);
            if (arch != null) {
                lblDetalleNombre.setText("Nombre: " + arch.getNombre());
                lblDetalleTipo.setText("Tipo: Archivo");
                lblDetalleTamano.setText("Tamaño: " + arch.getTamañoEnBloques() + " bloques");
                lblDetalleDueno.setText("Dueño: " + arch.getPropietario());
                return;
            }
        }

        if (textoNodo.startsWith("📁")) {
            Directorio dir = gestor.buscarDirectorioObj(nombreLimpio);
            if (dir != null) {
                lblDetalleNombre.setText("Nombre: " + dir.getNombre());
                lblDetalleTipo.setText("Tipo: Directorio");
                lblDetalleTamano.setText("Tamaño: " + dir.getArchivos().getTamano() + " archivos / "
                        + dir.getSubdirectorios().getTamano() + " carpetas");
                lblDetalleDueno.setText("Dueño: " + dir.getPropietario());
                return;
            }
        }

        limpiarDetalles();
    }

    private String limpiarTextoNodo(String textoNodo) {
        String limpio = textoNodo.replace("📁 ", "").replace("📄 ", "").trim();
        int indice = limpio.indexOf(" [");
        if (indice >= 0) {
            limpio = limpio.substring(0, indice).trim();
        }
        return limpio;
    }

    private void limpiarDetalles() {
        if (lblDetalleNombre != null) lblDetalleNombre.setText("Nombre: -");
        if (lblDetalleTipo != null) lblDetalleTipo.setText("Tipo: -");
        if (lblDetalleTamano != null) lblDetalleTamano.setText("Tamaño: -");
        if (lblDetalleDueno != null) lblDetalleDueno.setText("Dueño: -");
    }

    private Color colorPorNombre(String nombreArch) {
        int hash = Math.abs(nombreArch.hashCode());
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        return new Color(r % 150 + 100, g % 150 + 100, b % 150 + 100);
    }

    private String colorHexPorNombre(String nombreArch) {
        Color c = colorPorNombre(nombreArch);
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
