package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.JPA.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PokemonDAOImpl implements PokemonDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result Add(Pokemon pokemon) {
        Result result = new Result();
        try {
            String sql = "INSERT INTO POKEMON (ID, NOMBRE, TIPO, URLIMAGEN) VALUES (?, ?, ?, ?)";
            int rows = jdbcTemplate.update(sql, 
                pokemon.getId(), 
                pokemon.getNombre(), 
                pokemon.getTipo(), 
                pokemon.getUrlImagen()
            );
            
            if (rows > 0) {
                result.Correct = true;
                result.ErrorMessage = "Pokemon guardado con exito";
            }
        } catch (Exception ex) {
            result.Correct = false;
            result.ErrorMessage = ex.getMessage();
        }
        return result;
    }
}