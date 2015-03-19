import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz intermediaria entre el cliente y el servidor.
 * @author Juan Luis Navarro Rey.
 * @version 1.0
 */
public interface Proxy extends Remote{

    /**
     * Sube un fichero a la carpeta remota.
     * @param name Nombre del fichero que se quiere mandar.
     * @param contenido Contenido o parte del contenido del fichero.
     * @param actualizar Vale true si se va a actualizar el fichero y por tanto hay que borrar el fichero existente.
     * @return Devuelve true si el envio es correcto y false en caso contrario.
     * @throws RemoteException Lanza un error si se pierde la conexion o el fallo en el envio es grave.
     */
    RespuestaServidor<Boolean> subirFichero(String name, byte[]contenido, boolean actualizar) throws RemoteException;

    /**
     * Descarga un fichero a la carpeta local.
     * @param name Nombre del fichero que se quiere mandar.
     * @param index Numero de byte en el cual el servidor va a empezar a mandar el fichero.
     * @return Devuelve el contenido o parte del contenido del fichero.
     * @throws RemoteException Lanza un error si se pierde la conexion o el fallo en el envio es grave.
     */
    RespuestaServidor<byte[]> descargarFichero(String name, int index) throws RemoteException;

    /**
     * Modifica la carpeta remota del servidor.
     * @return Devuelve varios combobox para poder elegir la carpeta.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     * @version 2.0
     */
    RespuestaServidor<DefaultComboBoxModel<String>> modificaCarpetaServidor() throws RemoteException;

    /**
     * Modifica la carpeta remota del servidor.
     * @param carpetaServidor Nueva carpeta remota.
     * @return Devuelve true si se cambia correctamente la carpeta remota y false en caso contrario.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    RespuestaServidor<Boolean> modificaCarpetaServidor(String carpetaServidor) throws RemoteException;

    /**
     * Crea una lista con el nombre de todos los ficheros que existen el la carpeta remota del servidor.
     * @return Devuelve una lista con el nombre de los ficheros.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    RespuestaServidor<List<String>> listaFicherosCarpetaRemota() throws RemoteException;

    /**
     * Averigua la ultima modificacion de un fichero.
     * @param nombre Nombre del fichero.
     * @return Devuelve el tiempo de la ultima modificacion del fichero.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    RespuestaServidor<Long> ultimaModificacion(String nombre) throws RemoteException;

    /**
     * El servidor calcula la hora que tiene y la devuelve en milisegundos.
     * @return Devuelve la hora en milisegundos.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    RespuestaServidor<Long> getHoraServidor() throws RemoteException;

}
