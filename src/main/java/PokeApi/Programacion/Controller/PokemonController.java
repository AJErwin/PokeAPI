package PokeApi.Programacion.Controller;

import PokeApi.Programacion.DAO.UsuarioDAO;
import PokeApi.Programacion.JPA.Result;
import PokeApi.Programacion.ML.Pokemon;
import PokeApi.Programacion.ML.Usuario;
import PokeApi.Programacion.Service.EmailVerificationService;
import PokeApi.Programacion.Service.PokemonService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PokemonController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailVerificationService emailService;

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
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String region,
            Model model, Principal principal) {

        List<Pokemon> listaFinal = new ArrayList<>();
        boolean hasNext = false;

        // 1. Búsqueda por nombre
        if (nombre != null && !nombre.trim().isEmpty()) {
            List<Pokemon> resultados = pokemonService.buscarPokemon(nombre);
            listaFinal = resultados.stream().skip(offset).limit(limit).toList();
            hasNext = (offset + limit) < resultados.size();
        } 
        // 2. Búsqueda por tipo y/o región
        else if ((type != null && !type.equals("all")) || (region != null && !region.equals("all"))) {
            List<Pokemon> resultados = pokemonService.getByRegionAndType(
                    "all".equals(region) ? null : region, 
                    "all".equals(type) ? null : type
            );
            listaFinal = resultados.stream().skip(offset).limit(limit).toList();
            hasNext = (offset + limit) < resultados.size();
        } 
        // 3. Sin filtros (Pokedex normal)
        else {
            Result<Pokemon> apiResult = pokemonService.getPokemones(limit, offset);
            if (apiResult.Correct && apiResult.Objects != null) {
                listaFinal = apiResult.Objects;
                hasNext = apiResult.Objects.size() == limit;
            }
        }

        // Lógica de Favoritos (Botones capturar/atrapado)
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
        
        // Mantener filtros en la paginación
        model.addAttribute("nombreFiltro", nombre);
        model.addAttribute("tipoFiltro", type);
        model.addAttribute("regionFiltro", region);
        model.addAttribute("hasNext", hasNext);

        return "index";
    }

    @GetMapping("/pokedex/detalle/{id}")
    public String verDetalle(@PathVariable int id, Model model, Principal principal) {
        Pokemon pokemon = pokemonService.getById(id);
        String urlSonido = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/cries/" + id + ".ogg";
        pokemon.setUrlSonido(urlSonido);

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

        model.addAttribute("capturados", idsFavoritos); 
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
                                   Model model) throws MessagingException {

        // Validación de correo duplicado
        if (usuarioDAO.getByCorreo(correo) != null) {
            model.addAttribute("error", "EL CORREO YA ESTÁ EN USO");
            return "registro";
        }

        // Encriptar contraseña y Guardar usuario con STATUS=0
        String passwordEncriptado = passwordEncoder.encode(password);
        int resultado = usuarioDAO.guardarUsuario(username, correo, passwordEncriptado);
        
        if (resultado > 0) {
            Usuario usuario = usuarioDAO.getByCorreo(correo); 
            // Enviar correo Pokémon con token
            emailVerificationService.createToken(usuario.getIdUsuario(), correo);
            model.addAttribute("exito", "CUENTA CREADA. REVISA TU CORREO PARA ACTIVARLA.");
        } else {
            model.addAttribute("error", "ERROR AL GUARDAR EL USUARIO");
        }
        return "registro";
    }

    @GetMapping("/verify")
    public String verificarCuenta(@RequestParam("token") String token, Model model) {
        boolean validado = emailVerificationService.validateToken(token);
        if (validado) {
            model.addAttribute("exito");
        } else {
            model.addAttribute("error");
        }
        return "login"; 
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
    public String verRanking(
            @RequestParam(name = "orden", defaultValue = "desc") String orden, 
            @RequestParam(defaultValue = "20") int limit,   
            @RequestParam(defaultValue = "0") int offset,   
            Model model) {
        
        List<Pokemon> ranking = usuarioDAO.getFavoritosGlobales(orden, limit, offset);
        
        model.addAttribute("ranking", ranking);
        model.addAttribute("ordenActual", orden);
        model.addAttribute("limit", limit);
        model.addAttribute("currentOffset", offset);
        
        return "ranking";
    }

    @GetMapping("/pokedex/api/trivia-dia")
    @ResponseBody
    public Pokemon getTriviaJson() {
        int idAleatorio = (int) (Math.random() * 1010) + 1; // Usando el rango que tenías en la rama master
        return pokemonService.getById(idAleatorio);
    }

    @PostMapping("/pokedex/api/trivia-validar")
    @ResponseBody
    public Map<String, Object> validarTrivia(@RequestParam String nombreIntento, @RequestParam int idPokemon, Principal principal) {
        Pokemon p = pokemonService.getById(idPokemon);
        boolean esCorrecto = p.getNombre().equalsIgnoreCase(nombreIntento.trim());
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", esCorrecto);
        respuesta.put("nombreReal", p.getNombre().toUpperCase());

        if (principal != null) {
            Usuario usuario = usuarioDAO.getByUsernameOrCorreo(principal.getName());
            if (usuario != null) {
                if (esCorrecto) {
                    usuario.setRachaActual(usuario.getRachaActual() + 1);
                    if (usuario.getRachaActual() > usuario.getMaxRacha()) {
                        usuario.setMaxRacha(usuario.getRachaActual());
                    }
                } else {
                    usuario.setRachaActual(0);
                }
                
                usuarioDAO.updateUsuario(usuario); 
                respuesta.put("rachaActual", usuario.getRachaActual());
            }
        }
        return respuesta;
    }

    @GetMapping("/pokedex/api/trivia-ranking")
    @ResponseBody
    public List<Usuario> obtenerRankingTrivia() {
        return usuarioDAO.getTop5Rachas(); 
    }

    private String generarCodigo() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/recuperar")
    public String enviarCodigo(@RequestParam String correo, HttpSession session) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo=?";
        Integer existe = jdbcTemplate.queryForObject(sql, Integer.class, correo);

        if (existe != null && existe > 0) {
            String codigo = generarCodigo();

            session.setAttribute("codigo", codigo);
            session.setAttribute("correo", correo);

            emailService.enviarCodigo(correo, codigo);

            return "verificar-codigo";
        }
        return "forgot-password";
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam String codigo, HttpSession session) {
        String codigoSession = (String) session.getAttribute("codigo");

        if (codigo.equals(codigoSession)) {
            return "nueva-password";
        }

        return "verificar-codigo";
    }

    @PostMapping("/restablecer-password")
    public String restablecerPassword(@RequestParam String password, HttpSession session) {
        String correo = (String) session.getAttribute("correo");

        String passwordEncriptado = passwordEncoder.encode(password);

        String sql = "UPDATE usuario SET password=? WHERE correo=?";
        jdbcTemplate.update(sql, passwordEncriptado, correo);

        session.invalidate();

        return "redirect:/login";
    }
}