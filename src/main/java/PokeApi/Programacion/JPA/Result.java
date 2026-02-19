package PokeApi.Programacion.JPA;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class Result <T>{
    
    public boolean Correct;
    public String ErrorMessage;
     public Exception ex;
    public Object Object;
    public List<T> Objects;
    @JsonIgnore
    public int StatusCode;
    
}
