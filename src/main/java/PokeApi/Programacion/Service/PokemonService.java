package PokeApi.Programacion.Service;

import PokeApi.Programacion.ML.Pokemon;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

@Service
public class PokemonService {

    @Autowired
    private RestTemplate restTemplate;

    public List<Pokemon> GetAll(int limit, int offset) {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=" + limit + "&offset=" + offset; 
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

        return results.stream().map(p -> {
            Pokemon pokemon = new Pokemon();
            pokemon.setNombre(p.get("name"));
            return pokemon;
        }).collect(Collectors.toList());
    }
}