package PokeApi.Programacion.Service;

import PokeApi.Programacion.DAO.PokemonDAO;
import PokeApi.Programacion.JPA.Result;
import PokeApi.Programacion.ML.Pokemon;
import java.util.ArrayList;
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

    @Autowired
    private PokemonDAO pokemonDAO;
    

    public List<Pokemon> GetAll(int limit, int offset) {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=" + limit + "&offset=" + offset; 
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

        return results.parallelStream().map(p -> {
            Pokemon pokemon = new Pokemon();
            pokemon.setNombre(p.get("name"));
            String urlDetalle = p.get("url");
            String[] parts = urlDetalle.split("/");
            String id = parts[parts.length - 1];
            pokemon.setId(Integer.parseInt(id));
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            return pokemon;
        }).collect(Collectors.toList());
    }
    
    public List<Pokemon> buscarPokemon(String nombre) {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=1300&offset=0";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

        return results.parallelStream()
                .filter(p -> p.get("name").toLowerCase().contains(nombre.toLowerCase()))
                .map(p -> {
                    Pokemon pokemon = new Pokemon();
                    pokemon.setNombre(p.get("name"));
                    String urlDetalle = p.get("url");
                    String[] parts = urlDetalle.split("/");
                    String id = parts[parts.length - 1];
                    pokemon.setId(Integer.parseInt(id));
                    pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
                    return pokemon;
                })
                .collect(Collectors.toList());
    }

    public List<Pokemon> getByRegion(String region) {
        if (region == null || region.isBlank()) return new ArrayList<>();
        String regionUrl = "https://pokeapi.co/api/v2/region/" + region;
        Map<String, Object> regionResponse = restTemplate.getForObject(regionUrl, Map.class);
        List<Map<String, Object>> pokedexes = (List<Map<String, Object>>) regionResponse.get("pokedexes");
        if (pokedexes == null || pokedexes.isEmpty()) return new ArrayList<>();
        String pokedexUrl = (String) pokedexes.get(0).get("url");
        Map<String, Object> pokedexResponse = restTemplate.getForObject(pokedexUrl, Map.class);
        List<Map<String, Object>> entries = (List<Map<String, Object>>) pokedexResponse.get("pokemon_entries");
        List<Pokemon> lista = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            Map<String, Object> species = (Map<String, Object>) entry.get("pokemon_species");
            String name = (String) species.get("name");
            String url = (String) species.get("url");
            String cleanUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            String idStr = cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);
            int id = Integer.parseInt(idStr);
            Pokemon pokemon = new Pokemon();
            pokemon.setId(id);
            pokemon.setNombre(name);
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            lista.add(pokemon);
        }
        return lista;
    }

    public List<Pokemon> getByType(String type) {
        if (type == null || type.isBlank()) return new ArrayList<>();
        String url = "https://pokeapi.co/api/v2/type/" + type;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("pokemon") == null) return new ArrayList<>();
        List<Map<String, Object>> pokemonList = (List<Map<String, Object>>) response.get("pokemon");
        List<Pokemon> lista = new ArrayList<>();
        for (Map<String, Object> p : pokemonList) {
            Map<String, Object> poke = (Map<String, Object>) p.get("pokemon");
            String name = (String) poke.get("name");
            String pokeUrl = (String) poke.get("url");
            String cleanUrl = pokeUrl.endsWith("/") ? pokeUrl.substring(0, pokeUrl.length() - 1) : pokeUrl;
            String idStr = cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);
            int id = Integer.parseInt(idStr);
            Pokemon pokemon = new Pokemon();
            pokemon.setId(id);
            pokemon.setNombre(name);
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            lista.add(pokemon);
        }
        return lista;
    }

    public List<Pokemon> getByRegionAndType(String region, String type) {
        boolean tieneRegion = region != null && !region.isBlank();
        boolean tieneTipo = type != null && !type.isBlank();
        if (tieneRegion && !tieneTipo) return getByRegion(region);
        if (tieneTipo && !tieneRegion) return getByType(type);
        if (tieneRegion && tieneTipo) {
            List<Pokemon> porRegion = getByRegion(region);
            List<Pokemon> porTipo = getByType(type);
            List<Pokemon> resultado = new ArrayList<>();
            for (Pokemon r : porRegion) {
                for (Pokemon t : porTipo) {
                    if (r.getId() == t.getId()) resultado.add(r);
                }
            }
            return resultado;
        }
        return GetAll(8, 0);
    }

    public List<Pokemon> getByTwoTypes(String type1, String type2) {
        if (type1 == null || type1.isBlank() || type2 == null || type2.isBlank()) return new ArrayList<>();
        List<Pokemon> lista1 = getByType(type1);
        List<Pokemon> lista2 = getByType(type2);
        return lista1.stream()
                .filter(p1 -> lista2.stream().anyMatch(p2 -> p1.getId() == p2.getId()))
                .collect(Collectors.toList());
    }

    public Pokemon getById(int id) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + id;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Pokemon pokemon = new Pokemon();
        pokemon.setId(id);
        pokemon.setNombre((String) response.get("name"));
        pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
        
        List<Map<String, Object>> typesList = (List<Map<String, Object>>) response.get("types");
        String tipos = typesList.stream()
                .map(t -> (String) ((Map<String, Object>) t.get("type")).get("name"))
                .collect(Collectors.joining(", "));
        pokemon.setTipo(tipos);
        
        return pokemon;
    }

    public Result add(Pokemon pokemon) {
        return pokemonDAO.Add(pokemon);
    }
 

public Result Guardar(Pokemon pokemon) {
    return pokemonDAO.Add(pokemon);
}
}