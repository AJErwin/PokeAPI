package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.JPA.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PokemonDAOImpl implements PokemonDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int obtenerIdPorNombre(String username) {
        String sql = "SELECT IDUSUARIO FROM USUARIO WHERE USERNAME = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);
    }

    @Override
    public Result Add(Pokemon pokemon, int idUsuario) {
        Result result = new Result();
        try {
            String sql = "INSERT INTO FAVORITO (IDPOKEMON, POKEMON, IDUSUARIO) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 
                    pokemon.getId(), 
                    pokemon.getNombre(), 
                    idUsuario);
            result.Correct = true;
        } catch (Exception ex) {
            result.Correct = false;
            result.ErrorMessage = ex.getMessage();
        }
        return result;
    }
    
   @Override
public Result Delete(int idPokemon, int idUsuario) {
    Result result = new Result();
    try {
        String sql = "DELETE FROM FAVORITO WHERE IDPOKEMON = ? AND IDUSUARIO = ?";
        int filas = jdbcTemplate.update(sql, idPokemon, idUsuario);
        
        if (filas > 0) {
            result.Correct = true;
        } else {
            result.Correct = false;
            result.ErrorMessage = "No se encontró el registro.";
        }
    } catch (Exception ex) {
        result.Correct = false;
        result.ErrorMessage = ex.getMessage();
    }
    return result;
}

    @Override
    public List<Pokemon> obtenerTodosLosGuardados(int idUsuario) {
        String sql = "SELECT IDPOKEMON, POKEMON FROM FAVORITO WHERE IDUSUARIO = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Pokemon p = new Pokemon();
            p.setId(rs.getInt("IDPOKEMON"));
            p.setNombre(rs.getString("POKEMON"));
            p.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + p.getId() + ".png");
            return p;
        }, idUsuario);
    }
    
}