package controladores;

import org.bytedeco.javacv.Java2DFrameConverter;

import servicios.ServicioDecodificador;
import vistas.VistaReproductor;

import java.awt.image.BufferedImage;

public class ControladorReproductor {

    private final VistaReproductor vista;
    private final ServicioDecodificador servicio;

    public ControladorReproductor(VistaReproductor vista,
            ServicioDecodificador servicio) {
        this.vista = vista;
        this.servicio = servicio;
    }

    private void cicloRenderizado() {
        try (var conversor = new Java2DFrameConverter()) {
            while (servicio.isEjecutando()) {
                long inicio = System.currentTimeMillis();

                ServicioDecodificador.ContenedorFrame frameARenderizar = servicio.getSiguienteFrame();
                if (frameARenderizar != null) {
                    var ultimoTiempo = frameARenderizar.tiempo();

                    BufferedImage imgRenderizada = conversor.convert(frameARenderizar.frame());

                    vista.actualizarImagenVideo(imgRenderizada);

                    frameARenderizar.frame().close();
                }

            }
        }
    }
}
