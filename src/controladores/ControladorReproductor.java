package controladores;

import org.bytedeco.javacv.Java2DFrameConverter;

import servicios.ServicioDecodificador;
import vistas.VistaReproductor;

import java.awt.image.BufferedImage;

public class ControladorReproductor {

    private final VistaReproductor vista;
    private final ServicioDecodificador servicio;
    private long ultimoTiempo;

    public ControladorReproductor(VistaReproductor vista,
            ServicioDecodificador servicio) {
        this.vista = vista;
        this.servicio = servicio;

        this.vista.setSelecionarClick(evento -> iniciarProceso());
    }

    private void cicloRenderizado() {
        try (var conversor = new Java2DFrameConverter()) {
            while (servicio.isEjecutando()) {
                long inicio = System.currentTimeMillis();

                ServicioDecodificador.ContenedorFrame frameARenderizar = servicio.getSiguienteFrame();
                if (frameARenderizar != null) {
                    ultimoTiempo = frameARenderizar.tiempo();

                    BufferedImage imgRenderizada = conversor.convert(frameARenderizar.frame());

                    vista.actualizarImagenVideo(imgRenderizada);

                    frameARenderizar.frame().close();
                }

            }
        } catch (Exception ex) {
            System.err.println("[Controlador] Error en el ciclo de renderizado: " + ex.getMessage());
        }
    }

    private void cicloTelemetria() {
        while (servicio.isEjecutando()) {
            ServicioDecodificador.Metricas metricas = servicio.getMetricas(ultimoTiempo);
			vista.actualizarTelemetria(metricas);
            
        }
    }

    private void iniciarProceso() {
        var archivo = vista.solicitarArchivoVideo();
        if (archivo == null)
            return;

        // hilo PRODUCTOR (genera los frames a reproducir y los encola)
        new Thread(() -> servicio.iniciarDecodificacion(archivo.getAbsolutePath())).start();

        // hilo CONSUMIDOR (obtiene los frames encolados y los muestra)
        new Thread(this::cicloRenderizado).start();

        // hilo TELEMETRIA
        new Thread(this::cicloTelemetria, "Hilo Telemetria").start();


    }
}
