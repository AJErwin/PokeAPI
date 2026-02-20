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

    public Usuario getByUsername(String username) {
        String sql = "SELECT USERNAME, PASSWORD FROM USUARIOS WHERE USERNAME = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Usuario.class), username);
        } catch (Exception e) {
            return null;
        }
    }
}