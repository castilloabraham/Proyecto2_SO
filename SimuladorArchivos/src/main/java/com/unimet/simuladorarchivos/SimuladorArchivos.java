package com.unimet.simuladorarchivos;
import interfaz.VentanaPrincipal; 
import javax.swing.SwingUtilities;

public class SimuladorArchivos {

    public static void main(String[] args) {
        // 2. Le decimos a Java que abra nuestra ventana gráfica de forma segura
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Creamos la ventana y la hacemos visible
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            }
        });
    }
}