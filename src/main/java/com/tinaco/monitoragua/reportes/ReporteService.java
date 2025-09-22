package com.tinaco.monitoragua.reportes;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tinaco.monitoragua.nivelAguaActual.service.NivelAguaActualService;
import com.tinaco.monitoragua.nivelAguaHistorial.entity.NivelAguaHistorial;
import com.tinaco.monitoragua.nivelAguaHistorial.repository.NivelAguaHistorialRepository;
import com.tinaco.monitoragua.reportes.dto.ReporteTinacoViewModel;
import com.tinaco.monitoragua.tinaco.dto.TinacoResponse;
import com.tinaco.monitoragua.tinaco.entity.Tinaco;
import com.tinaco.monitoragua.tinaco.repository.TinacoRepository;
import com.tinaco.monitoragua.tinaco.service.TinacoService;
import com.tinaco.monitoragua.usuario.dto.UsuarioDataResponse;
import com.tinaco.monitoragua.usuario.entity.Usuario;
import com.tinaco.monitoragua.usuario.service.UsuarioService;

@Service
public class ReporteService {

    private final SpringTemplateEngine templateEngine;
    private final UsuarioService usuarioService;
    private final TinacoService tinacoService;
    private final NivelAguaActualService nivelActualService;
    private final NivelAguaHistorialRepository historialRepo;
    private final TinacoRepository tinacoRepository;

    public ReporteService(SpringTemplateEngine templateEngine,
                          UsuarioService usuarioService,
                          TinacoService tinacoService,
                          NivelAguaActualService nivelActualService,
                          NivelAguaHistorialRepository historialRepo,
                          TinacoRepository tinacoRepository) {
        this.templateEngine = templateEngine;
        this.usuarioService = usuarioService;
        this.tinacoService = tinacoService;
        this.nivelActualService = nivelActualService;
        this.historialRepo = historialRepo;
        this.tinacoRepository = tinacoRepository;
    }

    public byte[] generarPdf(Usuario usuario, Long tinacoId, int anio, int mes) throws Exception {
        YearMonth ym = YearMonth.of(anio, mes); // valida mes/anio automáticamente
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();

        UsuarioDataResponse u = usuarioService.obtenerDatosDeUsuario(usuario);
        TinacoResponse tinaco = tinacoService.obtenerTinacoPorId(tinacoId, usuario);
        var nivelActual = nivelActualService.obtenerUltimoNivel(tinacoId, usuario);

        LocalDateTime desde = first.atStartOfDay();
        LocalDateTime hasta = last.atTime(23, 59, 59);
        List<NivelAguaHistorial> puntos = historialRepo
                .findByTinacoIdAndFechaRegistroBetweenOrderByFechaRegistroAsc(tinacoId, desde, hasta);

        double consumoTotal = 0.0;
        double subidaTotal = 0.0;
        Set<LocalDate> diasConsumo = new HashSet<>();
        Set<LocalDate> diasSubida = new HashSet<>();
        if (puntos != null && puntos.size() > 1) {
            for (int i = 1; i < puntos.size(); i++) {
                var anterior = puntos.get(i - 1);
                var actual = puntos.get(i);
                double prev = anterior.getPorcentajeLlenado();
                double cur = actual.getPorcentajeLlenado();
                double delta = cur - prev; // positivo = subida, negativo = consumo
                LocalDate diaEvento = actual.getFechaRegistro().toLocalDate();
                if (delta < 0) {
                    double descenso = -delta; // porcentaje
                    if (descenso >= 5.0) {
                        double litros = tinaco.getCapacidadLitros() * (descenso / 100.0);
                        consumoTotal += litros;
                        diasConsumo.add(diaEvento);
                    }
                } else if (delta > 0) {
                    if (delta >= 5.0) {
                        double litros = tinaco.getCapacidadLitros() * (delta / 100.0);
                        subidaTotal += litros;
                        diasSubida.add(diaEvento);
                    }
                }
            }
        }

        int diasConConsumo = diasConsumo.size();
        int diasConSubida = diasSubida.size();
        double consumoPromedio = diasConConsumo > 0 ? (consumoTotal / diasConConsumo) : 0.0;
        double subidaPromedio = diasConSubida > 0 ? (subidaTotal / diasConSubida) : 0.0;

        ReporteTinacoViewModel vm = new ReporteTinacoViewModel();
        vm.usuarioNombre = u.getNombre();
        vm.usuarioEmail = u.getEmail();
        vm.fechaGeneracion = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now());
        vm.yearMonth = ym;
        vm.mesTexto = capitalizar(ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"))) + " " + anio;

        vm.tinacoId = tinaco.getId();
        vm.tinacoNombre = tinaco.getNombre();
        vm.tinacoUbicacion = tinaco.getUbicacion();
        vm.capacidadLitros = tinaco.getCapacidadLitros();
        vm.alturaMaximaCm = tinaco.getAlturaMaximaCm();
        vm.destinoAgua = tinaco.getDestinoAgua();

        vm.alturaActualCm = nivelActual.getAlturaCm();
        vm.porcentajeActual = nivelActual.getPorcentajeLlenado();

        Tinaco tinacoEntity = tinacoRepository.findById(tinacoId).orElse(null);
        if (tinacoEntity != null && tinacoEntity.getUsuario() != null
                && Objects.equals(tinacoEntity.getUsuario().getId(), usuario.getId())
                && tinacoEntity.getBomba() != null) {
            var b = tinacoEntity.getBomba();
            vm.bombaNombre = b.getNombre();
            vm.bombaCodigo = b.getDispositivo() != null ? b.getDispositivo().getCodigoIdentificador() : null;
            vm.bombaModo = b.getModoBomba() != null ? b.getModoBomba().name() : null;
            vm.bombaEstado = b.getEncendida() != null
                    ? (b.getEncendida() == com.tinaco.monitoragua.bomba.entity.Bomba.Encendida.TRUE ? "ENCENDIDA"
                            : "APAGADA")
                    : null;
        }

        vm.consumoTotalLitros = consumoTotal;
        vm.consumoPromedioDiarioLitros = consumoPromedio;
        vm.subidaTotalLitros = subidaTotal;
        vm.subidaPromedioDiariaLitros = subidaPromedio;

        Context ctx = new Context();
        ctx.setVariable("vm", vm);
        String html = templateEngine.process("reportes/reporte-tinaco", ctx);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String baseUri = Objects.requireNonNull(getClass().getResource("/static/")).toExternalForm();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, baseUri);
        builder.toStream(baos);
        builder.run();
        return baos.toByteArray();
    }

    private String capitalizar(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
