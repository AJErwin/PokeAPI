package PokeApi.Programacion.Controller;

import PokeApi.Programacion.DAO.UsuarioDAO;
import PokeApi.Programacion.JPA.Result;
import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.Usuario;
import PokeApi.Programacion.Service.EmailVerificationService;
import PokeApi.Programacion.Service.PokemonService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addGlobalAttributes(Authentication auth, Model model) {
        boolean esAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("esAdmin", esAdmin);
    }

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

        boolean hasNext = apiResult.Objects.size() == limit;

        model.addAttribute("pokemones", apiResult.Objects);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("urlBase", "/pokedex");
        model.addAttribute("hasNext", hasNext);

        return "index";
    }

    @GetMapping("/pokedex/buscar")
    public String buscarPokemon(@RequestParam String nombre,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit,
            Model model) {

        List<Pokemon> resultados = pokemonService.buscarPokemon(nombre);

        List<Pokemon> paginados = resultados.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        boolean hasNext = (offset + limit) < resultados.size();

        model.addAttribute("pokemones", paginados);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("nombre", nombre);
        model.addAttribute("urlBase", "/pokedex/buscar");
        model.addAttribute("hasNext", hasNext);

        return "index";
    }

    @GetMapping("/pokedex/dual")
    public String buscarDual(@RequestParam String type1,
            @RequestParam String type2,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit,
            Model model) {

        List<Pokemon> resultados = pokemonService.getByTwoTypes(type1, type2);

        List<Pokemon> paginados = resultados.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        boolean hasNext = (offset + limit) < resultados.size();

        model.addAttribute("pokemones", paginados);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("type1", type1);
        model.addAttribute("type2", type2);
        model.addAttribute("urlBase", "/pokedex/dual");
        model.addAttribute("hasNext", hasNext);

        return "index";
    }

    @GetMapping("/filtro")
    public String filtrar(@RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit,
            Model model) {

        List<Pokemon> resultados = pokemonService.getByRegionAndType(region, type);

        List<Pokemon> paginados = resultados.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        boolean hasNext = (offset + limit) < resultados.size();

        model.addAttribute("pokemones", paginados);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("region", region);
        model.addAttribute("type", type);
        model.addAttribute("urlBase", "/filtro");
        model.addAttribute("hasNext", hasNext);

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
        Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
        List<Pokemon> favoritos = pokemonService.obtenerTodosLosGuardados(usuario.getIdUsuario());
        model.addAttribute("favoritos", favoritos);
        model.addAttribute("usuario", usuario.getUsername());
        return "perfil";
    }

    @PostMapping("/pokedex/guardar")
    @ResponseBody
    public String guardar(@ModelAttribute Pokemon pokemon, Principal principal) {
        try {
            Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
            pokemonService.Guardar(pokemon, usuario.getIdUsuario());
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/pokedex/eliminar")
    @ResponseBody
    public String eliminarFavorito(@RequestParam("idPokemon") int idPokemon, Principal principal) {
        try {
            Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
            Result result = pokemonService.Delete(idPokemon, usuario.getIdUsuario());
            if (result.Correct) {
                return "OK";
            } else {
                return "Error: " + result.ErrorMessage;
            }
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String username,
            @RequestParam String correo,
            @RequestParam String password,
            Model model) {
        if (usuarioDAO.getByCorreo(correo) != null) {
            model.addAttribute("error", "EL CORREO YA ESTA EN USO");
            return "registro";
        }
        String passwordEncriptado = passwordEncoder.encode(password);
        int resultado = usuarioDAO.guardarUsuario(username, correo, passwordEncriptado);
        if (resultado > 0) {
            Usuario usuario = usuarioDAO.getByCorreo(correo);
            emailVerificationService.createToken(usuario.getIdUsuario(), correo);
            model.addAttribute("exito", "CUENTA CREADA. REVISA TU CORREO PARA ACTIVARLA.");
        } else {
            model.addAttribute("error", "ERROR AL GUARDAR EL USUARIO");
        }
        return "registro";
    }

    @GetMapping("/pokedex/usuarios")
    public String verUsuarios(Model model) {
        List<Usuario> usuarios = usuarioDAO.getAllUsuarios();
        for (Usuario usuario : usuarios) {
            List<Pokemon> favs = pokemonService.obtenerTodosLosGuardados(usuario.getIdUsuario());
            usuario.setFavoritos(favs);
        }
        model.addAttribute("usuarios", usuarios);
        return "usuarios";
    }

    @GetMapping("/pokedex/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") int id, Model model) {
        Usuario usuario = usuarioDAO.getById(id);
        if (usuario == null) {
            return "redirect:/pokedex/usuarios";
        }
        model.addAttribute("usuario", usuario);
        return "EditarUsuarios";
    }

    @PostMapping("/pokedex/usuarios/editar")
    public String guardarEdicionUsuario(@ModelAttribute Usuario usuario) {
        usuarioDAO.updateUsuario(usuario);
        return "redirect:/pokedex/usuarios";
    }

    @GetMapping("/pokedex/ranking")
    public String verRanking(@RequestParam(name = "orden", defaultValue = "desc") String orden, Model model) {
        List<Pokemon> ranking = usuarioDAO.getFavoritosGlobales(orden);
        for (Pokemon pokemon : ranking) {
            Pokemon datosApi = pokemonService.getById(pokemon.getId());
            if (datosApi != null) {
                pokemon.setNombre(datosApi.getNombre());
                pokemon.setUrlImagen(datosApi.getUrlImagen());
            } else {
                pokemon.setNombre("???");
                pokemon.setUrlImagen("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + pokemon.getId() + ".png");
            }
        }
        model.addAttribute("ranking", ranking);
        model.addAttribute("ordenActual", orden);
        return "ranking";
    }

    @GetMapping("/pokedex/api/trivia-dia")
    @ResponseBody
    public Pokemon getTriviaJson() {
        int idAleatorio = (int) (Math.random() * 1350) + 1;
        return pokemonService.getById(idAleatorio);
    }

    @PostMapping("/pokedex/api/trivia-validar")
    @ResponseBody
    public Map<String, Object> validarTrivia(@RequestParam String nombreIntento, @RequestParam int idPokemon) {
        Pokemon p = pokemonService.getById(idPokemon);
        boolean esCorrecto = p.getNombre().equalsIgnoreCase(nombreIntento.trim());
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", esCorrecto);
        respuesta.put("nombreReal", p.getNombre().toUpperCase());
        return respuesta;
    }
}
