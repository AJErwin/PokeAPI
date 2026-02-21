package PokeApi.Programacion.Controller;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.Service.PokemonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/pokedex")
    public String getAll(@RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Model model) {
        List<Pokemon> pokemones = pokemonService.GetAll(limit, offset);
        model.addAttribute("pokemones", pokemones);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", 8);
        return "index";
    }

    @GetMapping("/pokedex/buscar")
    public String buscarPokemon(@RequestParam String nombre, Model model) {
        List<Pokemon> resultados = pokemonService.buscarPokemon(nombre);
        List<Pokemon> limitados = resultados.stream().limit(8).toList();
        model.addAttribute("pokemones", limitados);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", 8);
        return "index";
    }

    @GetMapping("/pokedex/dual")
    public String buscarDual(@RequestParam String type1, @RequestParam String type2, Model model) {
        List<Pokemon> resultados = pokemonService.getByTwoTypes(type1, type2);
        List<Pokemon> limitados = resultados.stream().limit(8).toList();
        model.addAttribute("pokemones", limitados);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", 8);
        return "index";
    }

    @GetMapping("/filtro")
    public String filtrar(@RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            Model model) {
        List<Pokemon> resultados = pokemonService.getByRegionAndType(region, type);
        List<Pokemon> limitados = resultados.stream().limit(8).toList();
        model.addAttribute("pokemones", limitados);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", 8);
        return "index";
    }

    @GetMapping("/pokedex/detalle/{id}")
    public String verDetalle(@PathVariable int id, Model model) {
        Pokemon pokemon = pokemonService.getById(id);
        model.addAttribute("pokemon", pokemon);
        return "detalle";
    }
}
