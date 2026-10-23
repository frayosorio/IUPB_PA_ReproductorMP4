package servicios;


import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_core.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ServicioDetectorRostros {

    // Reducción al 50% de la imagen para que OpenCV sea 4x más rápido
    private static final double ESCALA = 0.5;
    private CascadeClassifier cascadaRostro;


    public ServicioDetectorRostros() {
        var contenidoArchivoClasificador = ServicioDetectorRostros.class.getResourceAsStream("/recursos/haarcascade_frontalface_default.xml");
        try {
            var archivoClasificador = File.createTempFile("haarcascade_frontalface", ".xml");
            archivoClasificador.deleteOnExit();
            Files.copy(contenidoArchivoClasificador, archivoClasificador.toPath(), StandardCopyOption.REPLACE_EXISTING);
            cascadaRostro = new CascadeClassifier(archivoClasificador.getAbsolutePath());

            System.out.println("[DetectorRostros] Clasificador Haar Cascade cargado correctamente.");
        } catch (Exception e) {
            System.err.println("[DetectorRostros] Error de inicialización: " + e.getMessage());
        }
    }

    public Frame detectarRostros(Frame frame) {
        if (frame == null || frame.image == null || cascadaRostro == null || cascadaRostro.empty()) {
            return frame;
        }
        var conversorImagenes = new OpenCVFrameConverter.ToMat();
        var imagenCV_Color = conversorImagenes.convert(frame);
        var imagenCV_Gris = new Mat();
        var imagenCV_Gris_Reducida = new Mat();

        opencv_imgproc.cvtColor(imagenCV_Color, imagenCV_Gris, opencv_imgproc.COLOR_BGR2GRAY);
        int alto = imagenCV_Gris.rows();
        int ancho = imagenCV_Gris.cols();
        opencv_imgproc.resize(imagenCV_Gris, imagenCV_Gris_Reducida,
                new Size((int) (ancho * ESCALA), (int) (alto * ESCALA)));

        opencv_imgproc.equalizeHist(imagenCV_Gris, imagenCV_Gris);

        var rostrosEncontrados = new RectVector();

        cascadaRostro.detectMultiScale(
                imagenCV_Gris_Reducida,
                rostrosEncontrados,
                1.1, //Factor de Escala
                3, // pixeles vecinos
                0,
                new Size(30, 30), // tamaño mínimo
                new Size(500, 500) // tamaño máximo
        );

        for (int i = 0; i < rostrosEncontrados.size(); i++) {
            var rectangulo = rostrosEncontrados.get(i);
            int x = (int) (rectangulo.x() / ESCALA);
            int y = (int) (rectangulo.y() / ESCALA);
            ancho = (int) (rectangulo.width() / ESCALA);
            alto = (int) (rectangulo.height() / ESCALA);
            opencv_imgproc.rectangle(imagenCV_Color,
                    new Point(x, y), // origen
                    new Point(x + ancho, y + alto), //destino
                    new Scalar(0, 255, 0, 0), // color del trazo
                    3, // grosor del trazo
                    opencv_imgproc.LINE_8,
                    0
            );
        }

        return conversorImagenes.convert(imagenCV_Color);
    }

}
