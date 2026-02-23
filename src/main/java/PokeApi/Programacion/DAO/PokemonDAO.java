package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.JPA.Result;
import java.util.List;

public interface PokemonDAO {

    int obtenerIdPorNombre(String username);

    Result Add(Pokemon pokemon, int idUsuario);

    Result Delete(int idPokemon, int idUsuario);

    List<Pokemon> obtenerTodosLosGuardados(int idUsuario);
}
