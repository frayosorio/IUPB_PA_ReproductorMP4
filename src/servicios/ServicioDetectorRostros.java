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

    public Frame detectarRostros(Frame frame){
        if(frame==null || frame.image==null || cascadaRostro==null || cascadaRostro.empty()){
            return  frame;
        }
        var conversorImagenes=new OpenCVFrameConverter.ToMat();
        var imagenCV_Color=conversorImagenes.convert(frame);
        var imagenCV_Gris=new Mat();

        opencv_imgproc.cvtColor(imagenCV_Color, imagenCV_Gris, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.equalizeHist(imagenCV_Gris, imagenCV_Gris);

        var rostrosEncontrados=new RectVector();

        cascadaRostro.detectMultiScale(
                imagenCV_Gris,
                rostrosEncontrados,
                1.1, //Factor de Escala
                3, // pixeles vecinos
                0,
                new Size(30, 30), // tamaño mínimo
                new Size(500, 500) // tamaño máximo
        );

        for(int i=0;i<rostrosEncontrados.size();i++){
            var rectangulo=rostrosEncontrados.get(i);
            opencv_imgproc.rectangle(imagenCV_Color,
                    new Point(rectangulo.x(), rectangulo.y()), // origen
                    new Point(rectangulo.x()+rectangulo.width(), rectangulo.y()+rectangulo.height()), //destino
                    new Scalar(0, 255, 0, 0), // color del trazo
                    3, // grosor del trazo
                    opencv_imgproc.LINE_8,
                    0
                    );
        }

        return conversorImagenes.convert(imagenCV_Color);
    }

}
