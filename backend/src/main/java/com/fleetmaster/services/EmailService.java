package com.fleetmaster.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String to, String code) {
        logger.info("--- EMAIL MOCK ---");
        logger.info("To: {}", to);
        logger.info("Code: {}", code);
        logger.info("------------------");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("FleetMaster Verification Code");
        msg.setText("Your verification code: " + code);
        try {
            mailSender.send(msg);
        } catch (Exception ex) {
            logger.warn("Email send failed, proceeding without email: {}", ex.getMessage());
        }
    }
}
