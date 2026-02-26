package PokeApi.Programacion.ML;

import java.util.List;

public class Pokemon {

    private int id;
    private String nombre;
    private String urlImagen;
    private String urlImagenShiny;
    private String Tipo;
    private List<Integer> estadisticas;
    private List<String> movimientos;
    private int cantidadFavoritos;

    public int getCantidadFavoritos() {
        return cantidadFavoritos;
    }

    public void setCantidadFavoritos(int cantidadFavoritos) {
        this.cantidadFavoritos = cantidadFavoritos;
    }

    public Pokemon() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getUrlImagenShiny() {
        return urlImagenShiny;
    }

    public void setUrlImagenShiny(String urlImagenShiny) {
        this.urlImagenShiny = urlImagenShiny;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String Tipo) {
        this.Tipo = Tipo;
    }

    public List<Integer> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(List<Integer> estadisticas) {
        this.estadisticas = estadisticas;
    }

    public List<String> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<String> movimientos) {
        this.movimientos = movimientos;
    }
}
