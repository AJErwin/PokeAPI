package PokeApi.Programacion.Service;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.JPA.Result;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

@Service
public class PokemonService {
    
    public Pokemon GetFromApi(String nombre) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + nombre.toLowerCase();
        RestTemplate restTemplate = new RestTemplate();
        Object response = restTemplate.getForObject(url, Object.class); 
        
        Pokemon pokemon = new Pokemon();
        pokemon.setNombre(nombre);
        return pokemon;
    }
}