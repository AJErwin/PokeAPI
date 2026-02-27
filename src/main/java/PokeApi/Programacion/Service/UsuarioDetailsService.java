package PokeApi.Programacion.Service;

import PokeApi.Programacion.DAO.UsuarioDAO;
import PokeApi.Programacion.ML.Usuario;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioDetailsService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioDAO.getByUsernameOrCorreo(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Credenciales no válidas");
        }

        if (usuario.getStatus() == 0) {
            throw new DisabledException("Cuenta no verificada");
        }

        String nombreRol = (usuario.getRol() != null) ? usuario.getRol().getNombreRol().toUpperCase() : "USER";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + nombreRol);

        return new org.springframework.security.core.userdetails.User(
                usuario.getCorreo(),
                usuario.getPassword(),
                Collections.singletonList(authority)
        );
    }
}

