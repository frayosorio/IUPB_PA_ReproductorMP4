package vistas;

import javax.swing.*;

import java.awt.*;
import java.io.File;

public class VistaReproductor extends JFrame {

    private final PanelVideo panelVideo = new PanelVideo();
    private final JLabel lblHeapMem = new JLabel("Heap JVM: 0 MB");
    private final JLabel lblOffHeapMem = new JLabel("Off-Heap / Nativa: 0 MB");
    private final JLabel lblCpuUso = new JLabel("Uso CPU: 0.0 %");
    private final JLabel lblTiempoLectura = new JLabel("Lectura I/O (FFmpeg): 0 ms");
    private final JLabel lblBufferEstado = new JLabel("Estado Búfer: 0 / 30");
    private final JProgressBar pbBuffer = new JProgressBar(0, 30);
    private final JButton btnSeleccionar = new JButton("Abrir MP4...");

    public VistaReproductor() {
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("Laboratorio MVC: Gestión de Recursos en Reproducción MP4");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(panelVideo, BorderLayout.CENTER);

        // Panel Lateral de Telemetría
        JPanel panelStats = new JPanel();
        panelStats.setLayout(new BoxLayout(panelStats, BoxLayout.Y_AXIS));
        panelStats.setBorder(BorderFactory.createTitledBorder(" Telemetría de Hardware "));
        panelStats.setPreferredSize(new Dimension(320, 0));

        Font fuenteStats = new Font("Monospaced", Font.BOLD, 12);
        Component[] componentes = {lblHeapMem, lblOffHeapMem, lblCpuUso, lblTiempoLectura, lblBufferEstado};
        
        for (Component comp : componentes) {
            comp.setFont(fuenteStats);
            panelStats.add(comp);
            panelStats.add(Box.createVerticalStrut(10));
        }

        pbBuffer.setStringPainted(true);
        panelStats.add(new JLabel("Ocupación Ring Buffer:"));
        panelStats.add(Box.createVerticalStrut(5));
        panelStats.add(pbBuffer);
        panelStats.add(Box.createVerticalStrut(20));
        panelStats.add(btnSeleccionar);

        add(panelStats, BorderLayout.EAST);
        setLocationRelativeTo(null);
    }

    public JButton getBtnSeleccionar() {
        return btnSeleccionar;
    }

    public File solicitarArchivoVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar video MP4 para prueba");
        int res = fileChooser.showOpenDialog(this);
        return (res == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    
}