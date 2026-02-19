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
    public String getAll(@RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Model model) {

        List<Pokemon> pokemones = pokemonService.GetAll(limit, offset);

        model.addAttribute("pokemones", pokemones);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);

        return "index";
    }

    //cambio mio (Mario region)
    @GetMapping("/region")
    public String getByRegion(@RequestParam String region, Model model) {

        List<Pokemon> pokemones = pokemonService.getByRegion(region);

        model.addAttribute("pokemones", pokemones);

        return "index";
    }

}
