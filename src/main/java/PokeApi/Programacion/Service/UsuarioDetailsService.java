package PokeApi.Programacion.Service;

import PokeApi.Programacion.DAO.UsuarioDAO;
import PokeApi.Programacion.ML.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioDAO.getByUsername(username);
        
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        return User.withUsername(usuario.getUsername())
                   .password(usuario.getPassword())
                   .roles("USER")
                   .build();
    }
}