package PokeApi.Programacion.ML;

public class Usuario {

    private int idUsuario;
    private String username;
    private String password;
    private String correo;
    private String fecharegistro;
    private int status;
    private int rolusuario;

    public Usuario() {
    }

    public Usuario(int idUsuario, String username, String password, String correo, String fecharegistro, int status, int rolusuario) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.correo = correo;
        this.fecharegistro = fecharegistro;
        this.status = status;
        this.rolusuario = rolusuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFecharegistro() {
        return fecharegistro;
    }

    public void setFecharegistro(String fecharegistro) {
        this.fecharegistro = fecharegistro;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRolusuario() {
        return rolusuario;
    }

    public void setRolusuario(int rolusuario) {
        this.rolusuario = rolusuario;
    }
}