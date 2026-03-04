package PokeApi.Programacion.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(email);
            helper.setSubject("⚔️ ¡Tu batalla Pokémon comienza ahora! ⚔️");

            String htmlContent
                    = "<!DOCTYPE html>"
                    + "<html>"
                    + "<body style='margin:0;padding:0;background:#0b3d0b;font-family:Courier New, monospace;'>"
                    + "<table align='center' width='700' cellpadding='0' cellspacing='0' style='margin-top:40px;border-collapse:collapse;'>"
                    + /* CABECERA */ "<tr>"
                    + "<td style='background:#b22222;padding:18px;text-align:center;border-top-left-radius:16px;border-top-right-radius:16px;'>"
                    + "<h1 style='color:white;margin:0;font-size:18px;'>🏆 Pokedex - Liga Pokémon 🏆</h1>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td style='background:black;height:6px;'></td></tr>"
                    + /* CAMPO */ "<tr>"
                    + "<td style='background:#4CAF50;padding:15px 30px;'>"
                    + "<table width='100%' cellpadding='0' cellspacing='0'>"
                    + /* ================= PIKACHU ================= */ "<tr>"
                    + "<td width='50%' style='vertical-align:top;color:white;'>"
                    + "<b>Mewtwo ♂ Lv.50</b><br>"
                    + "<div style='background:#ccc;width:180px;height:10px;border:2px solid #000;margin-top:4px;'>"
                    + "<div style='background:#00e676;width:150px;height:10px;'></div>"
                    + "</div>"
                    + "<div style='margin-top:6px;font-size:12px;'>"
                    + "<div>💥 Bola Sombra</div>"
                    + "<div>🌀 Recuperación</div>"
                    + "</div>"
                    + "</td>"
                    + "<td width='50%' align='center' style='vertical-align:bottom;'>"
                    + /* SPRITE BAJADO */ "<img src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/150.png' "
                    + "width='120' style='display:block;margin:0 auto -100px auto;'>"
                    + /* PLATAFORMA */ "<div style=\"width:140px;height:28px;background:white;border-radius:50%;margin:-20px auto 0 auto;\"></div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr><td colspan='2' height='10'></td></tr>"
                    + /* ================= CHARIZARD ================= */ "<tr>"
                    + "<td width='50%' align='center' style='vertical-align:bottom;'>"
                    + /* SPRITE BAJADO */ "<img src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/6.png' "
                    + "width='140' style='display:block;margin:0 auto -100px auto;'>"
                    + /* PLATAFORMA */ "<div style=\"width:170px;height:32px;background:white;border-radius:50%;margin:-20px auto 0 auto;\"></div>"
                    + "</td>"
                    + "<td width='50%' align='right' style='vertical-align:bottom;color:white;'>"
                    + "<b>Charizard ♂ Lv.45</b><br>"
                    + "<div style='background:#ccc;width:180px;height:10px;border:2px solid #000;margin-top:4px;'>"
                    + "<div style='background:#ff9100;width:140px;height:10px;'></div>"
                    + "</div>"
                    + "<div style='margin-top:6px;font-size:12px;text-align:right;'>"
                    + "<div>🔥 Lanzallamas</div>"
                    + "<div>🪽 Vuelo</div>"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + /* MENSAJE PROFESOR OAK */ "<tr>"
                    + "<td style='background:#f5f5f5;padding:20px;border-top:4px solid #000;'>"
                    + "<div style='background:white;border:3px solid #1a237e;padding:15px;font-size:13px;line-height:1.6;'>"
                    + "<b>Profesor Oak:</b><br><br>"
                    + "¡Hola Entrenador! Antes de que puedas desafiar oficialmente la Liga Pokémon,<br>"
                    + "debo verificar tu cuenta como Entrenador Pokémon.<br><br>"
                    + "Haz clic en el botón para confirmar tu registro y continuar tu aventura."
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + /* BOTÓN */ "<tr>"
                    + "<td style='background:white;text-align:center;padding:20px;border-bottom-left-radius:16px;border-bottom-right-radius:16px;'>"
                    + "<a href='" + link + "' style='display:inline-block;padding:14px 45px;background:#1a237e;color:#ffeb3b;"
                    + "text-decoration:none;font-weight:bold;border:4px solid #000;font-size:14px;border-radius:8px;'>"
                    + "⚡ CONFIRMAR CUENTA ⚡"
                    + "</a>"
                    + "<p style='font-size:11px;color:#444;margin-top:12px;'>Este enlace expira en 24 horas.</p>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</body></html>";

            helper.setText(htmlContent, true);
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
