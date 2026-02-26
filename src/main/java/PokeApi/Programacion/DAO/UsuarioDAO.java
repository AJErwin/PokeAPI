package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UsuarioDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Usuario getByUsernameOrCorreo(String identificador) {
        String sql = "SELECT * FROM USUARIO WHERE CORREO = ? OR USERNAME = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Usuario.class), identificador, identificador);
        } catch (Exception e) {
            return null;
        }
    }

    public Usuario getByCorreo(String correo) {
        String sql = "SELECT * FROM USUARIO WHERE CORREO = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Usuario.class), correo);
        } catch (Exception e) {
            return null;
        }
    }

    public int guardarUsuario(String username, String correo, String password) {
        String sql = "INSERT INTO USUARIO (USERNAME, CORREO, PASSWORD, STATUS, ROLUSUARIO) VALUES (?, ?, ?, 1, 2)";
        try {
            return jdbcTemplate.update(sql, username, correo, password);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Usuario> getAllUsuarios() {
        String sql = "SELECT * FROM USUARIO";
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Usuario.class));
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public List<Pokemon> getFavoritosGlobales() {
        String sql = "SELECT idPokemon AS id, nombre, urlImagen, COUNT(idPokemon) AS cantidadFavoritos FROM favoritos GROUP BY idPokemon, nombre, urlImagen ORDER BY cantidadFavoritos DESC";
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Pokemon.class));
        } catch (Exception e) {
            System.out.println("ERROR SQL EN getFavoritosGlobales: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}