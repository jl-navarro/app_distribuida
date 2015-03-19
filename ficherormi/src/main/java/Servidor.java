import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Implementacion del servidor.
 * @author Juan Luis Navarro Rey.
 * @version 1.0
 */
public class Servidor extends UnicastRemoteObject implements Proxy {

    // -Djava.rmi.server.codebase=file:///home/juan/IdeaProjects/ficherormi/target/classes -Djava.security.policy=file:///home/juan/IdeaProjects/ficherormi/target/classes/server.policy

    private Path carpertaServidor;
    private final int LONG_BUFFER = 1024;
    private final String CAR_ESCAPE = "#;;#";
    private final String SEP = System.getProperty("file.separator");

    /**
     * Inicializacion del servidor.
     * @throws RemoteException Lanza un error si se pierde la conexion o el fallo en el envio es grave.
     */
    protected Servidor() throws RemoteException {
        super();
        this.carpertaServidor = FileSystems.getDefault().getPath(System.getProperty("user.home"),"remoteFolder");
        if(Files.notExists(this.carpertaServidor)){
            try {
                Files.createDirectories(this.carpertaServidor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Lee un fichero.
     * @param file Fichero que se quiere leer.
     * @return Devuelve el contenido del fichero.
     * @throws Lanza una excepcion si no existe el fichero o no se puede leer.
     */
    private String leeFichero(Path file) throws IOException {
        byte[] contenido = Files.readAllBytes(file);
        return new String(contenido);
    }

    /*
     * Borra un fichero.
     * @param file Fichero que se quiere borrar.
     */
    private void borraFichero(Path file){

    }

    /**
     * Sube un fichero a la carpeta remota.
     * @param name Nombre del fichero que se quiere mandar.
     * @param contenido Contenido o parte del contenido del fichero.
     * @param actualizar Vale true si se va a actualizar el fichero y por tanto hay que borrar el fichero existente.
     * @return Devuelve true si el envio es correcto y false en caso contrario.
     * @throws RemoteException Lanza un error si se pierde la conexion o el fallo en el envio es grave.
     */
    @Override
    public RespuestaServidor<Boolean> subirFichero(String name, byte[] contenido, boolean actualizar) throws RemoteException {
        String path = this.carpertaServidor +SEP +name;
        Path ruta = FileSystems.getDefault().getPath(path);
        String contenidos = "";
        if(Files.notExists(ruta)){
            try {
                Files.createFile(ruta);
            } catch (IOException e) {
                return new RespuestaServidor<Boolean>(false,"El fichero no existe en la carpeta remota y además"
                    +" no se ha podido crear.");
            }
        }
        if(!actualizar){
            try {
                contenidos = leeFichero(ruta);
            } catch (IOException e) {
                e.printStackTrace();
                return new RespuestaServidor<Boolean>(false,e.toString());
            }
        }
        contenidos += new String(contenido);
        if(!Files.isWritable(ruta)){
            System.err.println("No se puede escribir");
            return new RespuestaServidor<Boolean>(false, "No tienes permisos de escritura");
        }
        try {
            Files.write(ruta,contenidos.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return new RespuestaServidor<Boolean>(false,e.toString());
        }
        return new RespuestaServidor<Boolean>(true,null);
    }

    /**
     * Descarga un fichero a la carpeta local.
     * @param name Nombre del fichero que se quiere mandar.
     * @param index Numero de byte en el cual el servidor va a empezar a mandar el fichero.
     * @return Devuelve el contenido o parte del contenido del fichero.
     * @throws RemoteException Lanza un error si se pierde la conexion o el fallo en el envio es grave.
     */
    @Override
    public RespuestaServidor<byte[]> descargarFichero(String name, int index) throws RemoteException {
        Path ruta = FileSystems.getDefault().getPath(this.carpertaServidor +SEP +name);
        int iteracion = (index / LONG_BUFFER) + 1;
        if(Files.notExists(ruta)){
            return new RespuestaServidor<byte[]>(null,"No existe el fichero");
        }
        try {
            byte[]contenidoFichero = Files.readAllBytes(ruta);
            byte[]contenido = null;
            if((iteracion*LONG_BUFFER) > contenidoFichero.length){
                contenido = new byte[contenidoFichero.length-index];
            }else {
                contenido = new byte[LONG_BUFFER];
            }
            for(int i=0; i<LONG_BUFFER && index<contenidoFichero.length; i++){
                contenido[i] = contenidoFichero[index];
                index ++;
            }
            if(index > contenidoFichero.length - LONG_BUFFER){
                String str = new String(contenido) +CAR_ESCAPE +"FIN";
                return new RespuestaServidor<byte[]>(str.getBytes(),null);
            }
            String str = new String(contenido) +CAR_ESCAPE +"continue";
            return new RespuestaServidor<byte[]>(str.getBytes(),null);
        } catch (IOException e) {
            return new RespuestaServidor<byte[]>(null,e.toString());
        }
    }

    /**
     * Modifica la carpeta remota del servidor.
     * @return Devuelve varios combobox para poder elegir la carpeta.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     * @version 2.0
     */
    @Override
    public RespuestaServidor<DefaultComboBoxModel<String>> modificaCarpetaServidor() throws RemoteException {
        DefaultComboBoxModel<String>modelo = new DefaultComboBoxModel<String>();
        JComboBox<String>comboBox = new JComboBox<String>(modelo);
        buscaDirectorios(modelo);
        return new RespuestaServidor<DefaultComboBoxModel<String>>(modelo,null);
    }

    private void buscaDirectorios(DefaultComboBoxModel<String>modelo){
        Path ruta = FileSystems.getDefault().getPath(System.getProperty("user.home"));
        File file = ruta.toFile();
        File[]files = file.listFiles();
        for(File f : files){
            if(f.isDirectory()){
                modelo.addElement(f.getAbsolutePath());
            }
        }
    }

    /**
     * Modifica la carpeta remota del servidor.
     * @param carpetaServidor Nueva carpeta remota.
     * @return Devuelve true si se cambia correctamente la carpeta remota y false en caso contrario.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    @Override
    public RespuestaServidor<Boolean> modificaCarpetaServidor(String carpetaServidor) throws RemoteException{
        this.carpertaServidor = Paths.get(carpetaServidor);
        System.out.println(this.carpertaServidor.toString());
        if(Files.notExists(this.carpertaServidor)){
            try {
                Files.createDirectories(this.carpertaServidor);
            } catch (IOException e) {
                new RespuestaServidor<Boolean>(false,"No se ha podido crear la carpeta en el servidor");
            }
        }
        return new RespuestaServidor<Boolean>(true,null);
    }

    /*
     * Lee todos los ficheros del directorio remoto incluyendo los subdirectorios.
     */
    private void leeFicheros(File file, List<String>listaFicheros){
        File[]lista = file.listFiles();
        for(File f : lista){
            if(f.isDirectory()){
                leeFicheros(f,listaFicheros);
            }else{
                listaFicheros.add(f.getName());
            }
        }
    }

    /**
     * Crea una lista con el nombre de todos los ficheros que existen el la carpeta remota del servidor.
     * @return Devuelve una lista con el nombre de los ficheros.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    @Override
    public RespuestaServidor<List<String>> listaFicherosCarpetaRemota() throws RemoteException {
        List<String>listaFicheros = new ArrayList<String>();
        leeFicheros(this.carpertaServidor.toFile(),listaFicheros);
        return new RespuestaServidor<List<String>>(listaFicheros,null);
    }

    /**
     * El servidor calcula la hora que tiene y la devuelve en milisegundos.
     * @return Devuelve la hora en milisegundos.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    @Override
    public RespuestaServidor<Long> getHoraServidor() throws RemoteException {
        return new RespuestaServidor<Long>(System.currentTimeMillis(),null);
    }

    /**
     * Averigua la ultima modificacion de un fichero.
     * @param nombre Nombre del fichero.
     * @return Devuelve el tiempo de la ultima modificacion del fichero.
     * @throws RemoteException Lanza un error si se pierde la conexion.
     */
    public RespuestaServidor<Long> ultimaModificacion(String nombre) throws RemoteException{
        Path ruta = FileSystems.getDefault().getPath(this.carpertaServidor.toString() +SEP +nombre);
        if(Files.notExists(ruta)){
            return new RespuestaServidor<Long>(null,"El fichero no existe");
        }
        long ft = -1;
        File file = ruta.toFile();
        ft = file.lastModified();
        return new RespuestaServidor<Long>(ft,null);
    }

    public static void main(String[]args) throws RemoteException, MalformedURLException {
        System.setSecurityManager(new SecurityManager());
        Servidor servidor = new Servidor();
        Naming.rebind("rmi://localhost:2005/Proxy",servidor);
        System.out.println("El servidor ya esta preparado");
    }
}
