package PokeApi.Programacion.Controller;

import PokeApi.Programacion.JPA.Result;
import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.Service.PokemonService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/pokedex")
public String mostrarPokedex(
        @RequestParam(defaultValue = "8") int limit,
        @RequestParam(defaultValue = "0") int offset,
        Model model) {

    Result<Pokemon> apiResult = pokemonService.getPokemones(limit, offset);
    
    model.addAttribute("pokemones", apiResult.Objects);
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

    @GetMapping("/pokedex/perfil")
    public String verPerfil(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        int idUsuario = pokemonService.obtenerIdPorUsername(principal.getName());
        List<Pokemon> favoritos = pokemonService.obtenerTodosLosGuardados(idUsuario);

        model.addAttribute("favoritos", favoritos);
        model.addAttribute("usuario", principal.getName());
        return "perfil";
    }

    @PostMapping("/pokedex/guardar")
    @ResponseBody
    public String guardar(@ModelAttribute Pokemon pokemon, Principal principal) {
        try {
            int idUsuario = pokemonService.obtenerIdPorUsername(principal.getName());
            pokemonService.Guardar(pokemon, idUsuario);
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/pokedex/eliminar")
    @ResponseBody
    public String eliminarFavorito(@RequestParam("idPokemon") int idPokemon, Principal principal) {
        try {
            int idUsuario = pokemonService.obtenerIdPorUsername(principal.getName());
            Result result = pokemonService.Delete(idPokemon, idUsuario);

            if (result.Correct) {
                return "OK";
            } else {
                return "Error: " + result.ErrorMessage;
            }
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }
}