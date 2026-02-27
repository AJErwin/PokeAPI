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

    public List<Pokemon> getFavoritosGlobales(String orden) {
        String direccion = "asc".equalsIgnoreCase(orden) ? "ASC" : "DESC";
        String sql = "SELECT idPokemon AS id, COUNT(idPokemon) AS cantidadFavoritos FROM favorito GROUP BY idPokemon ORDER BY cantidadFavoritos " + direccion;
        try {
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Pokemon.class));
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }


public Usuario getById(int id) {
        String sql = "SELECT * FROM USUARIO WHERE IDUSUARIO = ?";

try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Usuario.class  

), id);
        } catch (Exception e) {
            return null;
        }
    }

    public int updateUsuario(Usuario usuario) {
        String sql = "UPDATE USUARIO SET USERNAME = ?, CORREO = ?, STATUS = ?, ROLUSUARIO = ? WHERE IDUSUARIO = ?";
        try {
            return jdbcTemplate.update(sql,
                    usuario.getUsername(),
                    usuario.getCorreo(),
                    usuario.getStatus(),
                    usuario.getRolusuario(),
                    usuario.getIdUsuario());
        } catch (Exception e) {
            return 0;
        }
    }
    
    public Pokemon getPokemonDelDia() {
    long seed = java.time.LocalDate.now().toEpochDay();
    java.util.Random rand = new java.util.Random(seed);
    
    int idAleatorio = rand.nextInt(151) + 1;
    
    Pokemon pokemon = new Pokemon();
    pokemon.setId(idAleatorio);
    pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + idAleatorio + ".png");
    
    try {
        String sql = "SELECT POKEMON FROM favorito WHERE idPokemon = ? FETCH FIRST 1 ROWS ONLY";
        String nombreBase = jdbcTemplate.queryForObject(sql, String.class, idAleatorio);
        pokemon.setNombre(nombreBase);
    } catch (Exception e) {
        pokemon.setNombre("Desconocido");
    }
    
    return pokemon;
}
}
