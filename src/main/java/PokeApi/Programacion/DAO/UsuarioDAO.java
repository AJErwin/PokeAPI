package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.Usuario;
import PokeApi.Programacion.ML.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UsuarioDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Usuario getByUsernameOrCorreo(String identificador) {
        String sql = "SELECT U.*, R.NOMBREROL FROM USUARIO U "
                + "JOIN ROL R ON U.ROLUSUARIO = R.IDROL "
                + "WHERE U.CORREO = ? OR U.USERNAME = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("IDUSUARIO"));
                usuario.setUsername(rs.getString("USERNAME"));
                usuario.setPassword(rs.getString("PASSWORD"));
                usuario.setCorreo(rs.getString("CORREO"));
                usuario.setStatus(rs.getInt("STATUS"));
                usuario.setRolusuario(rs.getInt("ROLUSUARIO"));

                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("ROLUSUARIO"));
                rol.setNombreRol(rs.getString("NOMBREROL"));
                usuario.setRol(rol);

                return usuario;
            }, identificador, identificador);
        } catch (Exception e) {
            return null;
        }
    }

    public Usuario getByCorreo(String correo) {
        String sql = "SELECT U.*, R.NOMBREROL FROM USUARIO U "
                + "JOIN ROL R ON U.ROLUSUARIO = R.IDROL "
                + "WHERE U.CORREO = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("IDUSUARIO"));
                usuario.setUsername(rs.getString("USERNAME"));
                usuario.setPassword(rs.getString("PASSWORD"));
                usuario.setCorreo(rs.getString("CORREO"));
                usuario.setStatus(rs.getInt("STATUS"));

                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("ROLUSUARIO"));
                rol.setNombreRol(rs.getString("NOMBREROL"));
                usuario.setRol(rol);

                return usuario;
            }, correo);
        } catch (Exception e) {
            return null;
        }
    }

    public int guardarUsuario(String username, String correo, String password) {
        String sql = "INSERT INTO USUARIO (USERNAME, CORREO, PASSWORD, STATUS, ROLUSUARIO) VALUES (?, ?, ?, 0, 2)";
        try {
            return jdbcTemplate.update(sql, username, correo, password);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Usuario> getAllUsuarios() {
        String sql = "SELECT U.*, R.NOMBREROL FROM USUARIO U "
                + "JOIN ROL R ON U.ROLUSUARIO = R.IDROL";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("IDUSUARIO"));
                usuario.setUsername(rs.getString("USERNAME"));
                usuario.setCorreo(rs.getString("CORREO"));
                usuario.setStatus(rs.getInt("STATUS"));

                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("ROLUSUARIO"));
                rol.setNombreRol(rs.getString("NOMBREROL"));
                usuario.setRol(rol);

                return usuario;
            });
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public List<Pokemon> getFavoritosGlobales(String orden) {
        String direccion = "asc".equalsIgnoreCase(orden) ? "ASC" : "DESC";
        String sql = "SELECT idPokemon AS id, COUNT(idPokemon) AS cantidadFavoritos "
                + "FROM favorito GROUP BY idPokemon "
                + "ORDER BY cantidadFavoritos " + direccion;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Pokemon p = new Pokemon();
                p.setId(rs.getInt("id"));
                p.setCantidadFavoritos(rs.getInt("cantidadFavoritos"));
                return p;
            });
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public Usuario getById(int id) {
        String sql = "SELECT U.*, R.NOMBREROL FROM USUARIO U "
                + "JOIN ROL R ON U.ROLUSUARIO = R.IDROL "
                + "WHERE U.IDUSUARIO = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("IDUSUARIO"));
                usuario.setUsername(rs.getString("USERNAME"));
                usuario.setCorreo(rs.getString("CORREO"));
                usuario.setStatus(rs.getInt("STATUS"));
                usuario.setRolusuario(rs.getInt("ROLUSUARIO"));

                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("ROLUSUARIO"));
                rol.setNombreRol(rs.getString("NOMBREROL"));
                usuario.setRol(rol);

                return usuario;
            }, id);
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
            String sql = "SELECT NOMBRE FROM favorito WHERE idPokemon = ? FETCH FIRST 1 ROWS ONLY";
            String nombreBase = jdbcTemplate.queryForObject(sql, String.class, idAleatorio);
            pokemon.setNombre(nombreBase);
        } catch (Exception e) {
            pokemon.setNombre("Desconocido");
        }

        return pokemon;
    }
}
