import java.io.Serializable;

/**
 * Clase generica que contendra el valor de la respuesta del servidor y se le asocia un mensaje de error en caso de haberse
 * producido un error.
 * @author Juan Luis Navarro Rey.
 * @version 1.0
 */
public class RespuestaServidor<T> implements Serializable {

    /*
     * Valor generico de la respuesta del servidor
     */
    private T t;
    /*
     * Mensaje de error si ha producido algun error
     */
    private String error;

    /**
     * Constructor de la clase.
     * @param t Objeto genérico que se quiere inicializar.
     * @param errorMsg Mensaje de error.
     */
    public RespuestaServidor(T t, String errorMsg){
        this.t = t;
        this.error = errorMsg;
    }

    /**
     * Obtiene el valor del objeto genérico.
     * @return devuelve el valor del objeto genérico.
     */
    public T getValor(){
        return this.t;
    }

    /**
     * Obtiene el valor del mensaje de error.
     * @return devuelve el mensaje de error.
     */
    public String getError(){
        return this.error;
    }
}
