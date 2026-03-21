package interfaz;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    
    private JTree arbolDirectorios;

    public VentanaPrincipal() {
        // Configuración básica de la ventana
        setTitle("Simulador de Sistema de Archivos OS");
        setSize(900, 600); // Un poco más ancha para que quepa todo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla
        setLayout(new BorderLayout());

        // Llamamos al método que armará la interfaz
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // --- 1. SECCIÓN DEL ÁRBOL DE DIRECTORIOS (JTREE) ---
        // Creamos la raíz (el disco C:)
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("C: (Raíz)");
        
        // Creamos el componente JTree con esa raíz
        arbolDirectorios = new JTree(raiz);
        
        // Lo metemos en un panel con barra de desplazamiento (Scroll) por si crece mucho
        JScrollPane scrollArbol = new JScrollPane(arbolDirectorios);
        scrollArbol.setPreferredSize(new Dimension(250, 0));
        
        // Le ponemos un borde con título para que se vea profesional
        scrollArbol.setBorder(BorderFactory.createTitledBorder("Explorador de Archivos"));

        // Lo agregamos a la parte IZQUIERDA (WEST) de la ventana
        add(scrollArbol, BorderLayout.WEST);
        
        
        // --- 2. SECCIÓN CENTRAL (DISCO Y TABLA - PROVISIONAL) ---
        // Aquí irán los bloques de colores y la tabla de asignación más adelante
        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(40, 44, 52)); // Color oscuro moderno
        panelCentral.setBorder(BorderFactory.createTitledBorder("Simulación de Disco (SD)"));
        
        add(panelCentral, BorderLayout.CENTER);
    }

    // Método principal (main) para arrancar el programa
    public static void main(String[] args) {
        // Esto asegura que la interfaz gráfica inicie correctamente
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}