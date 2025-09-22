package com.tinaco.monitoragua.utils;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tinaco.monitoragua.exception.AlturaMaximaCmInvalidaException;
import com.tinaco.monitoragua.exception.CapacidadLitrosInvalidaException;
import com.tinaco.monitoragua.exception.CodigoIdentificadorInvalidoException;
import com.tinaco.monitoragua.exception.DestinoAguaInvalidoException;
import com.tinaco.monitoragua.exception.EmailInvalidoException;
import com.tinaco.monitoragua.exception.NombreInvalidoException;
import com.tinaco.monitoragua.exception.PasswordInvalidoException;
import com.tinaco.monitoragua.exception.UbicacionInvalidaException;

@Service
public class ValidationsService {

    private final Pattern EMAIL_PATTERN = Pattern
            .compile("^(?!.*\\.\\.)[a-zA-Z0-9][a-z0-9.+-_%]*@[a-z.-]+\\.[a-z]{2,}$");

    private final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,255}$");

    private void validarCamposVacios(String campo, String mensaje) {
        if (campo == null || campo.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje + " es obligatorio");
        }
    }

    public void validarRegistroUsuario(String nombre, String email, String password) throws IllegalArgumentException {
        validarCamposVacios(nombre, "El nombre de usuario");
        validarCamposVacios(email, "El email de usuario");
        validarCamposVacios(password, "La contraseña de usuario");

        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 255) {
            throw new EmailInvalidoException("El email no es valido o excede los 255 caracteres");
        }

        if (!NAME_PATTERN.matcher(nombre).matches()) {
            throw new NombreInvalidoException("El nombre de usuario debe tener entre 2 y 255 caracteres y sin simbolos");
        }

        if (password.length() < 8 || password.length() > 255) {
            throw new PasswordInvalidoException("La contraseña debe tener entre 8 y 255 caracteres");
        }
    }

    public void validarNombre(String nombre) throws IllegalArgumentException {
        validarCamposVacios(nombre, "El nombre de usuario");
        
        if (!NAME_PATTERN.matcher(nombre).matches()) {
            throw new NombreInvalidoException("El nombre de usuario debe tener entre 2 y 255 caracteres y sin simbolos");
        }
    }

    public void validarPassword(String password) throws IllegalArgumentException {
        validarCamposVacios(password, "La contraseña");

        if (password.length() < 8 || password.length() > 255) {
            throw new PasswordInvalidoException("La contraseña debe tener entre 8 y 255 caracteres");
        }
    }

    public void validarRegistroTinaco(String nombre, String ubicacion, Integer capacidadLitros, String destinoAgua,
            Double alturaMaximaCm, String codigoIdentificador) {
        validarCamposVacios(nombre, "El nombre del tinaco");
        validarCamposVacios(codigoIdentificador, "El código identificador del tinaco");

        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new NombreInvalidoException("El nombre del tinaco debe tener entre 2 y 100 caracteres");
        }

        if (ubicacion != null && (ubicacion.length() < 2 || ubicacion.length() > 100)) {
            throw new UbicacionInvalidaException("La ubicacion del tinaco debe tener entre 2 y 100 caracteres");
        }

        if (capacidadLitros == null || capacidadLitros <= 0 || capacidadLitros > 100000) {
            throw new CapacidadLitrosInvalidaException("La capacidad del tinaco es obligatorio y debe ser un número positivo y menor a 100000L");
        }

        if (destinoAgua != null && (destinoAgua.length() < 2 || destinoAgua.length() > 100)) {
            throw new DestinoAguaInvalidoException("El destino del agua debe tener entre 2 y 100 caracteres");
        }

        if (alturaMaximaCm == null || alturaMaximaCm <= 0 || alturaMaximaCm > 1000) {
            throw new AlturaMaximaCmInvalidaException("La altura máxima del tinaco es obligatoria y debe ser un número positivo y menor a 1000 cm");
        }
        
        if (codigoIdentificador == null || codigoIdentificador.trim().isEmpty() || codigoIdentificador.length() < 11 || codigoIdentificador.length() > 15) {
            throw new CodigoIdentificadorInvalidoException("El código identificador del tinaco es obligatorio y debe tener un minimo de 11 caracteres y un máximo de 15 caracteres");
        }
    }

    public void validarActualizarTinaco(String nombre, String ubicacion, Integer capacidadLitros, String destinoAgua,
            Double alturaMaximaCm) {
        validarCamposVacios(nombre, "El nombre del tinaco");

        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new NombreInvalidoException("El nombre del tinaco debe tener entre 2 y 100 caracteres");
        }
        if (ubicacion != null && (ubicacion.length() < 2 || ubicacion.length() > 100)) {
            throw new UbicacionInvalidaException("La ubicacion del tinaco debe tener entre 2 y 100 caracteres");
        }

        if (capacidadLitros == null || capacidadLitros <= 0 || capacidadLitros > 100000) {
            throw new CapacidadLitrosInvalidaException("La capacidad del tinaco es obligatorio y debe ser un número positivo y menor a 100000L");
        }

        if (destinoAgua != null && (destinoAgua.length() < 2 || destinoAgua.length() > 100)) {
            throw new DestinoAguaInvalidoException("El destino del agua debe tener entre 2 y 100 caracteres");
        }

        if (alturaMaximaCm == null || alturaMaximaCm <= 0 || alturaMaximaCm > 1000) {
            throw new AlturaMaximaCmInvalidaException("La altura máxima del tinaco es obligatoria y debe ser un número positivo y menor a 1000 cm");
        }
    }

    public void validarRegistroBomba(String nombre, String ubicacion, String codigoIdentificador) {
        validarCamposVacios(nombre, "El nombre de la bomba");
        validarCamposVacios(codigoIdentificador, "El código identificador de la bomba");

        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new NombreInvalidoException("El nombre de la bomba debe tener entre 2 y 100 caracteres");
        }

        if (ubicacion != null && (ubicacion.length() < 2 || ubicacion.length() > 100)) {
            throw new UbicacionInvalidaException("La ubicacion de la bomba debe tener entre 2 y 100 caracteres");
        }

        if (codigoIdentificador.length() < 11 || codigoIdentificador.length() > 15) {
            throw new CodigoIdentificadorInvalidoException("El código identificador de la bomba es obligatorio y debe tener un minimo de 11 caracteres y un máximo de 15 caracteres");
        }
    }

    public void validarActualizarBomba(String nombre, String ubicacion, Integer porcentajeEncender,
            Integer porcentajeApagar) {
        validarCamposVacios(nombre, "El nombre de la bomba");

        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new NombreInvalidoException("El nombre de la bomba debe tener entre 2 y 100 caracteres");
        }

        if (ubicacion != null && (ubicacion.length() < 2 || ubicacion.length() > 100)) {
            throw new UbicacionInvalidaException("La ubicacion de la bomba debe tener entre 2 y 100 caracteres");
        }

        if (porcentajeEncender == null || !List.of(15, 20, 25).contains(porcentajeEncender)) { // Con que uno sea veradadero, se lanza la excepción. Si el primero es falso, obligatoriamente se evaluará la siguiente condición.
            throw new IllegalArgumentException("El porcentaje para encender la bomba es obligatorio y debe ser 15, 20 o 25");
        }

        if (porcentajeApagar == null || !List.of(85, 90, 100).contains(porcentajeApagar)) {
            throw new IllegalArgumentException("El porcentaje para apagar la bomba es obligatorio y debe ser 85, 90 o 100");
        }
    }
}
