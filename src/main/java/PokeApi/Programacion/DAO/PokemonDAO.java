package PokeApi.Programacion.DAO;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.JPA.Result;

public interface PokemonDAO {
    Result Add(Pokemon pokemon);
}