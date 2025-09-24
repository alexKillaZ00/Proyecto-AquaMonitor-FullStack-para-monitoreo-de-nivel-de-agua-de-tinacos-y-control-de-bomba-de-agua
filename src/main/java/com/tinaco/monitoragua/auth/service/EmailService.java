package com.tinaco.monitoragua.auth.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

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

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = buildUrl("/reset-password.html?token=" + token);

        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(to);
        String subject = "Solicitud de Restablecimiento de Contraseña - AquaMonitor";
        Content content = new Content("text/plain",
                "Hola,\n\nHas solicitado restablecer tu contraseña en AquaMonitor.\n\n" +
                        "Haz clic en el siguiente enlace para continuar:\n" + resetUrl + "\n\n" +
                        "Este enlace expirará en 15 minutos por seguridad.\n\n" +
                        "Si no solicitaste esto, por favor ignora este correo.\n\n" +
                        "Gracias,\nEl equipo de AquaMonitor.");

        sendEmail(from, toEmail, subject, content);
    }

    public void sendEmailVerification(String email, String token) {
        String verificationUrl = buildUrl("/verify-email.html?token=" + token);

        Email from = new Email(fromEmail, fromName);
        Email toEmail = new Email(email);
        String subject = "Verificación de Correo Electrónico - AquaMonitor";
        Content content = new Content("text/plain",
                "¡Hola!\n\nGracias por registrarte en AquaMonitor.\n\n" +
                        "Por favor, haz clic en el siguiente enlace para verificar tu correo electrónico:\n"
                        + verificationUrl + "\n\n" +
                        "Este enlace expirará en 15 minutos.\n\n" +
                        "Si no te has registrado en AquaMonitor, por favor ignora este correo.\n\n" +
                        "Saludos,\nEl equipo de AquaMonitor");

        sendEmail(from, toEmail, subject, content);
    }

    private void sendEmail(Email from, Email to, String subject, Content content) {
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            // Log para debugging (opcional)
            System.out.println("SendGrid Response Status: " + response.getStatusCode());

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Error al enviar email. Status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar email a través de SendGrid", e);
        }
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