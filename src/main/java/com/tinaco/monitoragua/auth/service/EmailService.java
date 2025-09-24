package com.tinaco.monitoragua.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private int apiPort;

    @Value("${api.protocol}")
    private String apiProtocol;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        // La URL de tu frontend para la página de reseteo, si se sirve desde static,
        // usar localhost
        String resetUrl = buildUrl("/reset-password.html?token=" + token);

        // Si tienes un frontend separado, usar su URL
        // String resetUrl = "http://127.0.0.1:5500/reset-password.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromName + " <" + fromEmail + ">"); // El mismo correo de la configuración
        message.setTo(to);
        message.setSubject("Solicitud de Restablecimiento de Contraseña - AquaMonitor");
        message.setText("Hola,\n\nHas solicitado restablecer tu contraseña en AquaMonitor.\n\n" +
                "Haz clic en el siguiente enlace para continuar:\n" + resetUrl + "\n\n" +
                "Este enlace expirará en 15 minutos por seguridad.\n\n" +
                "Si no solicitaste esto, por favor ignora este correo.\n\n" +
                "Gracias,\nEl equipo de AquaMonitor.");

        mailSender.send(message);
    }

    public void sendEmailVerification(String email, String token) {
        String verificationUrl = buildUrl("/verify-email.html?token=" + token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromName + " <" + fromEmail + ">"); // El mismo correo de la configuración
        message.setTo(email);
        message.setSubject("Verificación de Correo Electrónico - AquaMonitor");
        message.setText("¡Hola!\n\nGracias por registrarte en AquaMonitor.\n\n" +
                "Por favor, haz clic en el siguiente enlace para verificar tu correo electrónico:\n" + verificationUrl+ "\n\n" +
                "Este enlace expirará en 15 minutos.\n\n" +
                "Si no te has registrado en AquaMonitor, por favor ignora este correo.\n\n" +
                "Saludos,\nEl equipo de AquaMonitor");
        mailSender.send(message);
    }

    private String buildUrl(String path) {
        // Para Railway, no incluir el puerto si es 443 (HTTPS) o 80 (HTTP)
        if ("https".equals(apiProtocol)) {
            return apiProtocol + "://" + apiHost + path;
        } else if ("http".equals(apiProtocol) && apiPort == 80) {
            return apiProtocol + "://" + apiHost + path;
        } else {
            return apiProtocol + "://" + apiHost + ":" + apiPort + path;
        }
    }
}