import vistas.VistaReproductor;

public class App {
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(() -> {
            VistaReproductor vista = new VistaReproductor();
            vista.setVisible(true);
        });
    }
}
