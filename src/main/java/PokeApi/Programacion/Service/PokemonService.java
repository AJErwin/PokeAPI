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

    public Result<Pokemon> getPokemones(int limit, int offset) {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=" + limit + "&offset=" + offset;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

        List<Pokemon> lista = results.parallelStream().map(p -> {
            Pokemon pokemon = new Pokemon();
            pokemon.setNombre(p.get("name"));
            String urlDetalle = p.get("url");
            String id = urlDetalle.split("/")[urlDetalle.split("/").length - 1];
            pokemon.setId(Integer.parseInt(id));
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            return pokemon;
        }).collect(Collectors.toList());

        Result<Pokemon> result = new Result<>();
        result.Correct = true;
        result.Objects = lista;
        return result;
    }

    public List<Pokemon> buscarCombinado(String nombre, String type, String region) {
        List<Pokemon> base;

        if (region != null && !region.isBlank() && !region.equals("all")) {
            base = getByRegion(region);
        } else {
            String url = "https://pokeapi.co/api/v2/pokemon?limit=1010&offset=0";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");
            base = results.parallelStream().map(p -> {
                Pokemon pokemon = new Pokemon();
                pokemon.setNombre(p.get("name"));
                String id = p.get("url").split("/")[p.get("url").split("/").length - 1];
                pokemon.setId(Integer.parseInt(id));
                pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
                return pokemon;
            }).collect(Collectors.toList());
        }

        if (type != null && !type.isBlank() && !type.equals("all")) {
            List<Pokemon> porTipo = getByType(type);
            base = base.stream()
                    .filter(p -> porTipo.stream().anyMatch(t -> t.getId() == p.getId()))
                    .collect(Collectors.toList());
        }

        if (nombre != null && !nombre.isBlank()) {
            String n = nombre.toLowerCase();
            base = base.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(n))
                    .collect(Collectors.toList());
        }

        return base;
    }

    public List<Pokemon> buscarPokemon(String nombre) {
        return buscarCombinado(nombre, null, null);
    }

    public List<Pokemon> getByRegion(String region) {
        if (region == null || region.isBlank() || region.equals("all")) return new ArrayList<>();
        String regionUrl = "https://pokeapi.co/api/v2/region/" + region;
        Map<String, Object> regionResponse = restTemplate.getForObject(regionUrl, Map.class);
        List<Map<String, Object>> pokedexes = (List<Map<String, Object>>) regionResponse.get("pokedexes");
        if (pokedexes == null || pokedexes.isEmpty()) return new ArrayList<>();

        String pokedexUrl = (String) pokedexes.get(0).get("url");
        Map<String, Object> pokedexResponse = restTemplate.getForObject(pokedexUrl, Map.class);
        List<Map<String, Object>> entries = (List<Map<String, Object>>) pokedexResponse.get("pokemon_entries");

        return entries.parallelStream().map(entry -> {
            Map<String, Object> species = (Map<String, Object>) entry.get("pokemon_species");
            String url = (String) species.get("url");
            String idStr = url.split("/")[url.split("/").length - 1];
            int id = Integer.parseInt(idStr);
            Pokemon pokemon = new Pokemon();
            pokemon.setId(id);
            pokemon.setNombre((String) species.get("name"));
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            return pokemon;
        }).collect(Collectors.toList());
    }

    public List<Pokemon> getByType(String type) {
        if (type == null || type.isBlank() || type.equals("all")) return new ArrayList<>();
        String url = "https://pokeapi.co/api/v2/type/" + type;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("pokemon") == null) return new ArrayList<>();

        List<Map<String, Object>> pokemonList = (List<Map<String, Object>>) response.get("pokemon");
        return pokemonList.parallelStream().map(p -> {
            Map<String, Object> poke = (Map<String, Object>) p.get("pokemon");
            String pokeUrl = (String) poke.get("url");
            String idStr = pokeUrl.split("/")[pokeUrl.split("/").length - 1];
            int id = Integer.parseInt(idStr);
            Pokemon pokemon = new Pokemon();
            pokemon.setId(id);
            pokemon.setNombre((String) poke.get("name"));
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
            return pokemon;
        }).collect(Collectors.toList());
    }

    public List<Pokemon> getByRegionAndType(String region, String type) {
        return buscarCombinado(null, type, region);
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

        List<Map<String, Object>> statsList = (List<Map<String, Object>>) response.get("stats");
        List<Integer> statsValores = statsList.stream()
                .map(s -> (Integer) s.get("base_stat"))
                .collect(Collectors.toList());
        pokemon.setEstadisticas(statsValores);

        List<Map<String, Object>> movesList = (List<Map<String, Object>>) response.get("moves");
        List<String> movimientos = movesList.stream()
                .limit(10)
                .map(m -> (String) ((Map<String, Object>) m.get("move")).get("name"))
                .collect(Collectors.toList());
        pokemon.setMovimientos(movimientos);

        Map<String, Object> sprites = (Map<String, Object>) response.get("sprites");
        pokemon.setUrlImagen((String) sprites.get("front_default"));
        pokemon.setUrlImagenShiny((String) sprites.get("front_shiny"));

        List<Map<String, Object>> typesList = (List<Map<String, Object>>) response.get("types");
        String tipos = typesList.stream()
                .map(t -> (String) ((Map<String, Object>) t.get("type")).get("name"))
                .collect(Collectors.joining(", "));
        pokemon.setTipo(tipos);

        return pokemon;
    }

    public Result Guardar(Pokemon pokemon, int idUsuario) {
        return pokemonDAO.Add(pokemon, idUsuario);
    }

    public List<Pokemon> obtenerTodosLosGuardados(int idUsuario) {
        return pokemonDAO.obtenerTodosLosGuardados(idUsuario);
    }

    public Result Delete(int idPokemon, int idUsuario) {
        return pokemonDAO.Delete(idPokemon, idUsuario);
    }

    public Pokemon getPokemonPorId(int idAleatorio) {
        Pokemon pokemon = getById(idAleatorio);
        if (pokemon != null) {
            pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + idAleatorio + ".png");
        }
        return pokemon;
    }
}