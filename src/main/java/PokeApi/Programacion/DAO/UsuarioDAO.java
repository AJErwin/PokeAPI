package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Usuario getByCorreo(String correo) {
        String sql = "SELECT * FROM USUARIO WHERE CORREO = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Usuario.class), correo);
        } catch (Exception e) {
            return null;
        }
    }

    public int guardarUsuario(String username, String correo, String password) {
        String sql = "INSERT INTO USUARIO (USERNAME, CORREO, PASSWORD, STATUS, ROLUSUARIO) VALUES (?, ?, ?, 1, 1)";
        try {
            return jdbcTemplate.update(sql, username, correo, password);
        } catch (Exception e) {
            return 0;
        }
    }
}