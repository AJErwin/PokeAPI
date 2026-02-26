package PokeApi.Programacion.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    public void createToken(Long userId, String email) {

        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);

        jdbcTemplate.update(
                "INSERT INTO EMAIL_VERIFICATION_TOKEN (TOKEN, USER_ID, EXPIRATION_DATE) VALUES (?, ?, ?)",
                token,
                userId,
                Timestamp.valueOf(expiration)
        );

        sendEmail(email, token);
    }

    private void sendEmail(String to, String token) {

        String link = appUrl + "/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verifica tu cuenta");
        message.setText("Haz clic para verificar tu cuenta: " + link);

        mailSender.send(message);
    }

    public boolean validateToken(String token) {

        String sql = "SELECT USER_ID, EXPIRATION_DATE FROM EMAIL_VERIFICATION_TOKEN WHERE TOKEN = ?";

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {

                Long userId = rs.getLong("USER_ID");
                Timestamp expiration = rs.getTimestamp("EXPIRATION_DATE");

                if (expiration.toLocalDateTime().isAfter(LocalDateTime.now())) {

                    jdbcTemplate.update(
                        "UPDATE USUARIO SET STATUS = 1 WHERE IDUSUARIO = ?", 
                        userId
                    );

                    jdbcTemplate.update(
                        "DELETE FROM EMAIL_VERIFICATION_TOKEN WHERE TOKEN = ?", 
                        token
                    );

                    return true;
                }
            }
            return false;
        }, token);
    }
}