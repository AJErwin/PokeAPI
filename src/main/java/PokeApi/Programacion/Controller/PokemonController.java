package PokeApi.Programacion.Controller;

import PokeApi.Programacion.DAO.UsuarioDAO;
import PokeApi.Programacion.JPA.Result;
import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.Usuario;
import PokeApi.Programacion.Service.EmailVerificationService;
import PokeApi.Programacion.Service.PokemonService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String region,
            Model model, Principal principal) {

        List<Pokemon> listaFinal = new ArrayList<>();

        if ((nombre != null && !nombre.isEmpty())
                || (type != null && !type.equals("all") && !type.isEmpty())
                || (region != null && !region.equals("all") && !region.isEmpty())) {

            List<Pokemon> resultados = pokemonService.buscarCombinado(nombre, type, region);
            listaFinal = resultados.stream().skip(offset).limit(limit).collect(Collectors.toList());
            model.addAttribute("totalResultados", resultados.size());
        } else {
            Result<Pokemon> apiResult = pokemonService.getPokemones(limit, offset);
            if (apiResult.Correct && apiResult.Objects != null) {
                listaFinal = apiResult.Objects;
            }
        }

        List<Integer> idsFavoritos = new ArrayList<>();
        if (principal != null) {
            Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
            if (usuario != null) {
                List<Pokemon> favoritos = pokemonService.obtenerTodosLosGuardados(usuario.getIdUsuario());
                if (favoritos != null) {
                    for (Pokemon fav : favoritos) {
                        idsFavoritos.add(fav.getId());
                    }
                }
            }
        }

        model.addAttribute("pokemones", listaFinal);
        model.addAttribute("capturados", idsFavoritos);
        model.addAttribute("currentOffset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("nombreFiltro", nombre);
        model.addAttribute("tipoFiltro", type);
        model.addAttribute("regionFiltro", region);

        return "index";
    }

    @GetMapping("/pokedex/detalle/{id}")
    public String verDetalle(@PathVariable int id, Model model, Principal principal) { // Agrega Principal aquí
        Pokemon pokemon = pokemonService.getById(id);
        String urlSonido = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/cries/" + id + ".ogg";
        pokemon.setUrlSonido(urlSonido);

        // NUEVA LÓGICA: Verificar favoritos para el botón de la vista de detalle
        List<Integer> idsFavoritos = new ArrayList<>();
        if (principal != null) {
            Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
            if (usuario != null) {
                List<Pokemon> favoritos = pokemonService.obtenerTodosLosGuardados(usuario.getIdUsuario());
                if (favoritos != null) {
                    for (Pokemon fav : favoritos) {
                        idsFavoritos.add(fav.getId());
                    }
                }
            }
        }

        model.addAttribute("capturados", idsFavoritos); // Enviamos la lista
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
            return result.Correct ? "OK" : "Error: " + result.ErrorMessage;
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String username, @RequestParam String correo, @RequestParam String password, Model model) {
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
            usuario.setFavoritos(pokemonService.obtenerTodosLosGuardados(usuario.getIdUsuario()));
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
            }
        }
        model.addAttribute("ranking", ranking);
        model.addAttribute("ordenActual", orden);
        return "ranking";
    }

    @GetMapping("/pokedex/api/trivia-dia")
    @ResponseBody
    public Pokemon getTriviaJson() {
        int idAleatorio = (int) (Math.random() * 1010) + 1;
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
