package PokeApi.Programacion.Controller;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.Service.PokemonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/pokedex")
    public String getAll(@RequestParam(defaultValue = "22") int limit,
                         @RequestParam(defaultValue = "0") int offset, 
                         Model model) {
        List<Pokemon> pokemones = pokemonService.GetAll(limit, offset);
        model.addAttribute("pokemones", pokemones);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        return "index";
    }

    @GetMapping("/pokedex/buscar")
    public String buscarPokemon(@RequestParam String nombre, Model model) {
        List<Pokemon> resultados = pokemonService.buscarPokemon(nombre);
        model.addAttribute("pokemones", resultados);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", 22);
        return "index";
    }

    @GetMapping("/region")
    public String getByRegion(@RequestParam String region, Model model) {
        List<Pokemon> pokemones = pokemonService.getByRegion(region);
        model.addAttribute("pokemones", pokemones);
        return "index";
    }

    @GetMapping("/type")
    public String getByType(@RequestParam String type, Model model) {
        List<Pokemon> pokemones = pokemonService.getByType(type);
        model.addAttribute("pokemones", pokemones);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", pokemones.size());
        return "index";
    }

    @GetMapping("/filtro")
    public String filtrar(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            Model model) {
        List<Pokemon> pokemones = pokemonService.getByRegionAndType(region, type);
        model.addAttribute("pokemones", pokemones);
        model.addAttribute("currentOffset", 0);
        model.addAttribute("limit", pokemones.size());
        return "index";
    }
}