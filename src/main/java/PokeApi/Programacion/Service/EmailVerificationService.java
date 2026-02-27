package PokeApi.Programacion.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
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

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String emailFrom;

    public void createToken(int userId, String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);

        jdbcTemplate.update(
                "INSERT INTO EMAIL_VERIFICATION_TOKEN (TOKEN, USER_ID, EXPIRATION_DATE) VALUES (?, ?, ?)",
                token,
                userId,
                Timestamp.valueOf(expiration)
        );

        String link = appUrl + "/verify?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Verifica tu cuenta - Pokedex System");
            message.setText("¡Bienvenido Entrenador!\n\nHaz clic en el siguiente enlace para activar tu cuenta:\n\n" + link);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        String sql = "SELECT USER_ID, EXPIRATION_DATE FROM EMAIL_VERIFICATION_TOKEN WHERE TOKEN = ?";

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                Long userId = rs.getLong("USER_ID");
                Timestamp expiration = rs.getTimestamp("EXPIRATION_DATE");

                if (expiration.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    int filas = jdbcTemplate.update(
                            "UPDATE USUARIO SET STATUS = 1 WHERE IDUSUARIO = ?",
                            userId
                    );

                    jdbcTemplate.update(
                            "DELETE FROM EMAIL_VERIFICATION_TOKEN WHERE TOKEN = ?",
                            token
                    );

                    return filas > 0;
                }
            }
            return false;
        }, token);
    }
}