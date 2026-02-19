package PokeApi.Programacion.Controller;

import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.PokemonListResponse;
import PokeApi.Programacion.Service.PokemonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/api/pokemon/all")
    public List<Pokemon> getAll(@RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return pokemonService.GetAll(limit, offset);
    }
}
