package com.tinaco.monitoragua.reportes;

import java.time.DateTimeException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tinaco.monitoragua.usuario.entity.Usuario;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping(value = "/tinaco/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarPdfTinaco(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam("tinacoId") Long tinacoId,
            @RequestParam("anio") int anio,
            @RequestParam("mes") int mes) {
        try {
            byte[] pdf = reporteService.generarPdf(usuario, tinacoId, anio, mes);
            String filename = "reporte_tinaco_" + tinacoId + "_" + anio + "_" + String.format("%02d", mes) + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (DateTimeException ex) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body("Parámetros de fecha inválidos".getBytes());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage().getBytes());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN)
                    .body("No se pudo generar el reporte".getBytes());
        }
    }
}
