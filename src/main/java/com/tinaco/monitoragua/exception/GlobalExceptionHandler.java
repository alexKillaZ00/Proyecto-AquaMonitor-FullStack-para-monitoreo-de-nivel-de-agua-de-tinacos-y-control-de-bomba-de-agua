package com.tinaco.monitoragua.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EmailYaRegistradoException.class)
        public ResponseEntity<?> handleEmailYaRegistrado(EmailYaRegistradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT) // 409
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(CredencialesInvalidasException.class)
        public ResponseEntity<?> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED) // 401
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(NombreInvalidoException.class)
        public ResponseEntity<?> handleNombreInvalido(NombreInvalidoException ex) {
                return ResponseEntity.unprocessableEntity().body(Map.of( // 422
                                "timestamp", LocalDateTime.now(),
                                "message", ex.getMessage()));
        }

        @ExceptionHandler(EmailInvalidoException.class)
        public ResponseEntity<?> handleEmailInvalido(EmailInvalidoException ex) {
                return ResponseEntity.unprocessableEntity().body(Map.of( // 422
                                "timestamp", LocalDateTime.now(),
                                "message", ex.getMessage()));
        }

        @ExceptionHandler(PasswordInvalidoException.class)
        public ResponseEntity<?> handlePasswordInvalido(PasswordInvalidoException ex) {
                return ResponseEntity.unprocessableEntity().body(Map.of( // 422
                                "timestamp", LocalDateTime.now(),
                                "message", ex.getMessage()));
        }

        @ExceptionHandler(TinacoNoEncontradoException.class)
        public ResponseEntity<?> handleTinacoNoEncontrado(TinacoNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(UsuarioNoEncontradoException.class)
        public ResponseEntity<?> handleTinacoNoEncontrado(UsuarioNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(NivelActualNoEncontradoException.class)
        public ResponseEntity<?> handleTinacoNoEncontrado(NivelActualNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(DispositivoNoEncontradoException.class)
        public ResponseEntity<?> handleTinacoNoEncontrado(DispositivoNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(BombaNoEncontradaException.class)
        public ResponseEntity<?> handleTinacoNoEncontrado(BombaNoEncontradaException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(RefreshTokenNoEncontradoException.class)
        public ResponseEntity<?> handleRefreshTokenNoEncontrado(RefreshTokenNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND) // 404
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(RefreshTokenExpiradoException.class)
        public ResponseEntity<?> handleRefreshTokenExpirado(RefreshTokenExpiradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED) // 401
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(RefreshTokenInvalidoException.class)
        public ResponseEntity<?> handleRefreshTokenInvalido(RefreshTokenInvalidoException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED) // 401
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }       

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST) // 400
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(AccesoDenegadoPorRoleException.class)
        public ResponseEntity<?> handleAccesoDenegadoPorRole(AccesoDenegadoPorRoleException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN) // 403
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(AccessTokenNoEncontradoException.class)
        public ResponseEntity<?> handleAccessTokenNoEncontrado(AccessTokenNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED) // 401
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        // --- Tinaco/Dispositivo/Bomba reglas de negocio ---
        @ExceptionHandler(DispositivoYaAsignadoException.class)
        public ResponseEntity<?> handleDispositivoYaAsignado(DispositivoYaAsignadoException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT) // 409
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(RecursoNoPerteneceAlUsuarioException.class)
        public ResponseEntity<?> handleRecursoNoPertenece(RecursoNoPerteneceAlUsuarioException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN) // 403
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(TinacoYaTieneBombaException.class)
        public ResponseEntity<?> handleTinacoYaTieneBomba(TinacoYaTieneBombaException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT) // 409
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(BombaYaAsociadaATinacoException.class)
        public ResponseEntity<?> handleBombaYaAsociada(BombaYaAsociadaATinacoException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT) // 409
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(TinacoSinBombaAsociadaException.class)
        public ResponseEntity<?> handleTinacoSinBomba(TinacoSinBombaAsociadaException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT) // 409
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }

        // --- Validaciones de campos Tinaco ---
        @ExceptionHandler({
                        UbicacionInvalidaException.class,
                        DestinoAguaInvalidoException.class,
                        CapacidadLitrosInvalidaException.class,
                        AlturaMaximaCmInvalidaException.class,
                        CodigoIdentificadorInvalidoException.class})
        public ResponseEntity<?> handleTinacoFieldValidation(RuntimeException ex) {
                return ResponseEntity.unprocessableEntity().body(Map.of( // 422
                                "timestamp", LocalDateTime.now(),
                                "message", ex.getMessage()));
        }

        // Controlador genérico de errores
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleGenericException(Exception ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                                .body(Map.of("timestamp", LocalDateTime.now(),
                                                "message", ex.getMessage()));
        }
}
