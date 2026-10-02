package controladores;

import org.bytedeco.javacv.Java2DFrameConverter;

import servicios.ServicioDecodificador;
import servicios.TiempoServicio;
import vistas.VistaReproductor;

import java.awt.image.BufferedImage;

public class ControladorReproductor {

    private final int TIEMPO_PAUSA = 200; // Muestreo 5 veces por segundo
    private final int TIEMPO_FRAME = 33; // 30 FPS -> 1000 ms/30

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

                ServicioDecodificador.ContenedorFrame contenedorARenderizar = servicio.getSiguienteFrame();
                if (contenedorARenderizar != null) {
                    ultimoTiempo = contenedorARenderizar.tiempo();

                    BufferedImage imgRenderizada = conversor.convert(contenedorARenderizar.frame());
                    vista.actualizarImagenVideo(imgRenderizada);
                    contenedorARenderizar.frame().close();
                }
                //hacer pausa de acuerdo al tiempo real de reproducción
                //30 FPS
                long tiempoTranscurrido = System.currentTimeMillis() - inicio;
                long tiempoPausa = Math.max(0, TIEMPO_FRAME - tiempoTranscurrido);
                //Thread.sleep(tiempoPausa);
                TiempoServicio.pausarMilisegundos(tiempoPausa);
            }
        } catch (Exception ex) {
            System.err.println("[Controlador] Error en el ciclo de renderizado: " + ex.getMessage());
        }
    }

    private void cicloTelemetria() {
        while (servicio.isEjecutando()) {
            ServicioDecodificador.Metricas metricas = servicio.getMetricas(ultimoTiempo);
            vista.actualizarTelemetria(metricas);
            TiempoServicio.pausarMilisegundos(TIEMPO_PAUSA);
        }
    }

    private void iniciarProceso() {
        var archivo = vista.solicitarArchivoVideo();
        if (archivo == null)
            return;

        // hilo PRODUCTOR (genera los frames a reproducir y los encola)
        new Thread(() -> servicio.iniciarDecodificacion(archivo.getAbsolutePath()), "Hilo Decodificacion").start();

        // hilo CONSUMIDOR (obtiene los frames encolados y los muestra)
        new Thread(this::cicloRenderizado, "Hilo Renderizado").start();

        // hilo TELEMETRIA
        new Thread(this::cicloTelemetria, "Hilo Telemetria").start();


    }
}
