package servicios;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import com.sun.management.OperatingSystemMXBean;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class ServicioDecodificador {

    public static final int TAMAÑO_MAXIMO_COLA = 30;
    private final OperatingSystemMXBean beanSO = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private boolean ejecutando = false;

    public record ContenedorFrame(Frame frame, long tiempo) {
    }

    public record Metricas(long memoriaHeapMB, long memoriaOffHeapMB,
            double porcentajeUsoCPU, int ocupacionCola,
            long tiempoLectura) {
    }

    private final BlockingQueue<ContenedorFrame> cola = new ArrayBlockingQueue<>(TAMAÑO_MAXIMO_COLA);

    public boolean isEjecutando() {
        return ejecutando;
    }

    public void iniciarDecodificacion(String rutaVideo) {
        ejecutando = true;
        try {
            var grabber = new FFmpegFrameGrabber(rutaVideo);
            grabber.start();

            Frame frameOriginal;
            while ((frameOriginal = grabber.grabFrame()) != null) {
                long tiempoInicial = System.nanoTime();
                Frame frameClonado = frameOriginal.clone();
                long tiempo = (System.nanoTime() - tiempoInicial);

                cola.put(new ContenedorFrame(frameClonado, tiempo));
            }
        } catch (Exception ex) {
            System.err.println("Error en el SERVICIO de Decodificación: " + ex.getMessage());
        } finally {
            ejecutando = false;
        }
    }

    public ContenedorFrame getSiguienteFrame() {
        return cola.poll();
    }

    public void detener() {
        this.cola.clear();
    }

    public Metricas getMetricas(long tiempoLectura) {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1048576;

        long memoriaHeapMB = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        long memoriaProceso = beanSO.getCommittedVirtualMemorySize() / mb;
        long memoriaOffHeapMB = Math.max(0, memoriaProceso - memoriaHeapMB);
        double porcentajeUsoCPU = beanSO.getCpuLoad() * 100;

        return new Metricas(memoriaHeapMB, memoriaOffHeapMB, porcentajeUsoCPU, cola.size(), tiempoLectura);

    }

}
