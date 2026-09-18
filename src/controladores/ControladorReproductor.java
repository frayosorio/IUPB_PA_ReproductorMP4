package controladores;

import servicios.ServicioDecodificador;
import vistas.VistaReproductor;

public class ControladorReproductor {

    private final VistaReproductor vista;
    private final ServicioDecodificador servicio;

    public ControladorReproductor(VistaReproductor vista,
            ServicioDecodificador servicio) {
        this.vista = vista;
        this.servicio = servicio;
    }
}
