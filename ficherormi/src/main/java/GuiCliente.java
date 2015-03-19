
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import javax.swing.*;
/*
 * Created by JFormDesigner on Sat Nov 15 18:10:23 CET 2014
 */


/**
 * Interfaz grafica que interactua con el usuario
 * @author Juan Luis Navarro Rey
 * @version 1.0
 */
public class GuiCliente extends JFrame {

    private Proxy proxy;
    private Path carpetaLocal;
    private Reloj relojCliente;
    private long tmin;
    private long errorCristian;
    private final int LONG_BUFFER_SERVIDOR = 1024;
    private final String CAR_ESCAPE = "#;;#";

    /**
     * Constructor de la clase.
     * @param proxy Interfaz que conectara el cliente con el servidor.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public GuiCliente(Proxy proxy) throws RemoteException, NotBoundException, MalformedURLException {
        initComponents();
        this.proxy = proxy;
        this.relojCliente = new Reloj();
        errorCristian = 0;
        tmin = 0;
        this.carpetaLocal = FileSystems.getDefault().getPath(System.getProperty("user.home"),"localFolder");
        if(Files.notExists(this.carpetaLocal)){
            try {
                Files.createDirectories(this.carpetaLocal);
                this.carpetaLocal.toFile().setLastModified(relojCliente.getTime());
            } catch (IOException e) {
                operacionErronea(e.toString());
            }
        }
        sincronizaRelojActionPerformed(null);
        this.setSize(700, 400);
    }

    private void operacionExitosa(String msg){
        JOptionPane.showMessageDialog(null, msg, "EXITO", JOptionPane.INFORMATION_MESSAGE);
    }

    private void operacionErronea(String error){
        JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    private void SalirActionPerformed(ActionEvent e) {
        System.exit(0);
    }

    private File eligeCarpeta(){
        JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File carpeta = null;
        int returnVal = jfc.showOpenDialog(jfc);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            carpeta = jfc.getSelectedFile();
        }
        return carpeta;
    }

    private void cambiarCarpetaServidorActionPerformed(ActionEvent e) {
        RespuestaServidor<DefaultComboBoxModel<String>> respuesta = null;
        JComboBox<String>combo = null;
        DefaultComboBoxModel modelo = null;
        try {
            respuesta = proxy.modificaCarpetaServidor();
            if(respuesta.getError() != null){
                operacionErronea(respuesta.getError());
                return;
            }
            modelo = respuesta.getValor();
            combo = new JComboBox<String>(modelo);
            new ElegirCarpetaRemota(combo.getModel(),this.proxy);
        } catch (RemoteException e1) {
            operacionErronea(e1.toString());
        }

    }

    private void infoActionPerformed(ActionEvent e) {
        String mensaje = "Aplicación de prácticas de la asignatura SSD"
                +"\nAutor: Juan Luis Navarro Rey"
                +"\nCurso académico: 2014-2015";
        JOptionPane.showMessageDialog(null,mensaje,"Información",JOptionPane.INFORMATION_MESSAGE);
    }

    private void eligeCarpetaLocalActionPerformed(ActionEvent e) {
        File nuevaCarpetaLocal = eligeCarpeta();
        if (nuevaCarpetaLocal == null) {
            return;
        }
        this.carpetaLocal = FileSystems.getDefault().getPath(nuevaCarpetaLocal.getAbsolutePath());
        if(Files.notExists(this.carpetaLocal)){
            try {
                Files.createDirectories(this.carpetaLocal);
                this.carpetaLocal.toFile().setLastModified(relojCliente.getTime());
            } catch (IOException e1) {
                operacionErronea(e1.toString());
            }
        }
    }

    private void verRutaLocalActionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null,this.carpetaLocal,"Carpeta local",JOptionPane.INFORMATION_MESSAGE);
    }

    private byte[] leeContenidoFichero(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private RespuestaServidor<Boolean> mandarFicheroAlServidorDeTamanioGrande(String nombre, byte[]contenidoFichero,int longFichero) throws RemoteException {
        byte[]buffer = null;
        boolean envioCompletado = false;
        boolean actualizar = true;
        int index = 0;
        int iteraciones = 1;
        RespuestaServidor<Boolean> respuesta = null;
        while(!envioCompletado){
            if(longFichero < (iteraciones*LONG_BUFFER_SERVIDOR) ){
                buffer = new byte[longFichero - (iteraciones*LONG_BUFFER_SERVIDOR)];
            }else {
                buffer = new byte[LONG_BUFFER_SERVIDOR];
            }
            for(int i=0; (i<buffer.length && index<contenidoFichero.length); i++){
                buffer[i] = contenidoFichero[index];
                index ++;
            }
            respuesta = proxy.subirFichero(nombre,buffer,actualizar);
            if(actualizar){
                actualizar = false;
            }
            if(index >= contenidoFichero.length - 1){
                envioCompletado = true;
            }
        }
        return respuesta;
    }

    private boolean upload(File file) throws IOException {
        boolean resultado = false;
        RespuestaServidor<Boolean>respuesta = null;
        if (!file.isDirectory()) {
            byte[]contenidoFichero = leeContenidoFichero(FileSystems.getDefault().getPath(file.getAbsolutePath()));
            if (contenidoFichero.length > LONG_BUFFER_SERVIDOR) {
                respuesta = mandarFicheroAlServidorDeTamanioGrande(file.getName(),contenidoFichero,contenidoFichero.length);
                resultado = respuesta.getValor();
            }else{
                respuesta = proxy.subirFichero(file.getName(),contenidoFichero,true);
                resultado = respuesta.getValor();
            }
        }
        if(!resultado){
            operacionErronea(respuesta.getError());
            return false;
        }
        return true;
    }

    private void subirFicheroActionPerformed(ActionEvent e) {
        RespuestaServidor<Boolean>respuesta = null;
        if(Files.notExists(this.carpetaLocal)){
            operacionErronea("No existe la carpeta local");
            return;
        }
        try {
            File[] files = this.carpetaLocal.toFile().listFiles();
            boolean resultado = true;
            for (File file : files) {
                resultado = upload(file);
                if(!resultado){
                    return;
                }
            }
        }catch(IOException ioe){
            operacionErronea(ioe.toString());
        }
        operacionExitosa("Se han subido correctamente los ficheros");
    }

    private String descargaFichero(String name){
        try {
            RespuestaServidor<byte[]> respuesta = null;
            String cad = "";
            String texto = "";
            int index = 0;
            while(!cad.equals("FIN")) {
                respuesta = proxy.descargarFichero(name,index);
                byte[] contenido = respuesta.getValor();
                if (contenido == null) {
                    operacionErronea(respuesta.getError());
                    return null;
                } else {
                    String[]sep = new String(contenido).split(CAR_ESCAPE);
                    cad = sep[1];
                    texto += sep[0];
                    index += LONG_BUFFER_SERVIDOR;
                }
            }
            return texto;
        } catch (RemoteException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private void descargarFicheroActionPerformed(ActionEvent e) {
        RespuestaServidor<List<String>> respuesta = null;
        List<String>ficheros = null;
        try {
            respuesta = proxy.listaFicherosCarpetaRemota();
            ficheros = respuesta.getValor();
        } catch (RemoteException e1) {
            operacionErronea(e1.toString());
            return;
        }
        for(String nombre : ficheros){
            String texto = descargaFichero(nombre);
            Path fichero = FileSystems.getDefault().getPath(this.carpetaLocal +System.getProperty("file.separator") +nombre);
            File file = fichero.toFile();
            if(Files.notExists(fichero)){
                try {
                    Files.createFile(fichero);
                    fichero.toFile().setLastModified(relojCliente.getTime());
                } catch (IOException e1) {
                    operacionErronea(e1.toString());
                    return;
                }
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(texto);
                bw.close();
                fichero.toFile().setLastModified(relojCliente.getTime());
            } catch (IOException e1) {
                operacionErronea(e1.toString());
            }
        }
        operacionExitosa("Se han descargado todos los ficheros correctamente");
    }

    private HashMap<String, Boolean> ficheroModernoClienteServidor(List<Path> locales, List<String> remotos){
        HashMap<String,Boolean> moderno = new HashMap<String,Boolean>();
        RespuestaServidor<Long>respuesta = null;
        for(String nombreRemoto : remotos){
            for(Path pathLocal : locales){
                if(nombreRemoto.equals(pathLocal.getFileName().toString())){
                    try {
                        respuesta = proxy.ultimaModificacion(nombreRemoto);
                        long ft = respuesta.getValor();
                        if((Files.getLastModifiedTime(pathLocal).toMillis() - errorCristian) > ft){
                            moderno.put(nombreRemoto, true);
                        }else if((Files.getLastModifiedTime(pathLocal).toMillis() + errorCristian) < ft){
                            moderno.put(pathLocal.getFileName().toString(), false);
                        }
                    } catch (RemoteException e) {
                        operacionErronea(e.toString());
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return moderno;
    }

    private void subirDescargarFicheros(HashMap<String,Boolean>mapa){
        Iterator<String>ficheros = mapa.keySet().iterator();
        if (ficheros == null) {
            operacionErronea("No se han podido actualizar los directorios");
            return;
        }
        boolean subir = false;
        String fileName = "";
        while(ficheros.hasNext()){
            fileName = ficheros.next();
            if(!mapa.get(fileName)){
                //descargar fichero del servidor
                String textoFichero = descargaFichero(fileName);
                Path fichero = FileSystems.getDefault().getPath(this.carpetaLocal.toString(),fileName);
                try {
                    File file = fichero.toFile();
                    FileWriter fw = new FileWriter(file);
                    fw.write(textoFichero);
                    fw.close();
                } catch (IOException e) {
                    operacionErronea("No se ha podido actualizar el fichero " +fichero.getFileName());
                }
            }else{
                //subir fichero al servidor
                try {
                    boolean res = upload(new File(this.carpetaLocal +File.separator +fileName));
                    if(!res){
                        return;
                    }
                } catch (IOException e) {
                    operacionErronea(e.toString());
                }
            }
        }
    }

    private boolean actualizarFicherosComunes(){
        RespuestaServidor<List<String>>respuesta = null;
        try {
            respuesta = proxy.listaFicherosCarpetaRemota();
            if(respuesta.getError() != null){
                operacionErronea(respuesta.getError());
                return false;
            }
            List<String>ficherosRemotos = respuesta.getValor();
            File[]ficheros = this.carpetaLocal.toFile().listFiles();
            List<Path>ficherosLocales = new ArrayList<Path>(ficheros.length);
            for(File file : ficheros){
                ficherosLocales.add(FileSystems.getDefault().getPath(file.getAbsolutePath()));
            }
            HashMap<String,Boolean> actualizar = ficheroModernoClienteServidor(ficherosLocales,ficherosRemotos);
            subirDescargarFicheros(actualizar);
        } catch (RemoteException e1) {
            operacionErronea(e1.toString());
            return false;
        }
        return true;
    }

    private HashMap<String,Boolean> ficherosNoCmunesClienteServidor(List<String>ficherosLocales,List<String>ficherosRemotos){
        HashMap<String,Boolean> noComunes = new HashMap<String, Boolean>();
        for(String file : ficherosLocales) {
            if (!ficherosRemotos.contains(file)) {
                noComunes.put(file,true);
            }
        }
        for(String file : ficherosRemotos){
            if(!ficherosLocales.contains(file)){
                noComunes.put(file,false);
            }
        }
        return noComunes;
    }

    private boolean actualizarFicherosNoComunes(){
        RespuestaServidor<List<String>>respuesta = null;
        try {
            respuesta = proxy.listaFicherosCarpetaRemota();
            if(respuesta.getError() != null){
                operacionErronea(respuesta.getError());
                return false;
            }
            List<String>ficherosRemotos = respuesta.getValor();
            File[]ficheros = this.carpetaLocal.toFile().listFiles();
            List<String>ficherosLocales = new ArrayList<String>(ficheros.length);
            for(File file : ficheros){
                ficherosLocales.add(file.getName());
            }
            HashMap<String,Boolean> noComunes = ficherosNoCmunesClienteServidor(ficherosLocales,ficherosRemotos);
            subirDescargarFicheros(noComunes);
        } catch (RemoteException e) {
            operacionErronea(e.toString());
            return false;
        }
        return true;
    }

    private void actualizarDirectoriosActionPerformed(ActionEvent e) {
        boolean ok = actualizarFicherosComunes();
        if(ok){
            operacionExitosa("se han actualizado los ficheros comunes del cliente y del servidor");
        }else{
            operacionErronea("No se ha podido actualizar los ficheros comunes del cliente y del servidor");
        }
        ok = actualizarFicherosNoComunes();
        if(ok){
            operacionExitosa("se han actualizado los ficheros no comunes del cliente y del servidor");
        }else{
            operacionErronea("No se ha podido actualizar los ficheros no comunes del cliente y del servidor");
        }
    }

    private void sincronizaRelojActionPerformed(ActionEvent e) {
        RespuestaServidor<Long>respuesta = null;
        Reloj relojAuxiliar = new Reloj();
        relojAuxiliar.setTime(relojCliente.getTime());
        try {
            long empiezaSinc = relojAuxiliar.getTime();
            respuesta = proxy.getHoraServidor();
            long acabaSinc = relojAuxiliar.getTime();
            if(respuesta.getError() != null){
                operacionErronea(respuesta.getError());
                return;
            }
            long sinc = respuesta.getValor() + ((acabaSinc - empiezaSinc)/2);
            relojCliente.setTime(sinc);
            errorCristian = ((acabaSinc - empiezaSinc)/2) - tmin;
        } catch (RemoteException e1) {
            operacionErronea(e1.toString());
        }
        operacionExitosa("El cliente se ha sincronizado con el servidor");
    }

    private void tminActionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null,tmin,"Valor de Tmin",JOptionPane.INFORMATION_MESSAGE);
    }

    private void cambiarTminActionPerformed(ActionEvent e) {
        String strEntrada = JOptionPane.showInputDialog(null,"Escribe el nuevo valor de Tmin");
        if(strEntrada == null){
            return;
        }
        long entrada = Long.parseLong(strEntrada);
        if(entrada < 0){
            operacionErronea("El valor de Tmin no es válido. Tiene que ser mayor o igual que cero.");
            return;
        }
        tmin = entrada;
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - juan navarro
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menu2 = new JMenu();
        menuItem4 = new JMenuItem();
        menuItem3 = new JMenuItem();
        menuItem7 = new JMenuItem();
        menuItem2 = new JMenuItem();
        menu3 = new JMenu();
        menuItem5 = new JMenuItem();
        menuItem6 = new JMenuItem();
        menuItem1 = new JMenuItem();
        panel1 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();
        button4 = new JButton();
        button3 = new JButton();

        //======== this ========
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplicaci\u00f3n SSD");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("Archivo");

                //======== menu2 ========
                {
                    menu2.setText("Ajustes");

                    //---- menuItem4 ----
                    menuItem4.setText("Cambiar carpeta local");
                    menuItem4.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            eligeCarpetaLocalActionPerformed(e);
                        }
                    });
                    menu2.add(menuItem4);

                    //---- menuItem3 ----
                    menuItem3.setText("Cambiar carpeta remota");
                    menuItem3.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cambiarCarpetaServidorActionPerformed(e);
                        }
                    });
                    menu2.add(menuItem3);

                    //---- menuItem7 ----
                    menuItem7.setText("Cambiar Tmin");
                    menuItem7.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cambiarTminActionPerformed(e);
                        }
                    });
                    menu2.add(menuItem7);
                }
                menu1.add(menu2);

                //---- menuItem2 ----
                menuItem2.setText("Salir");
                menuItem2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SalirActionPerformed(e);
                    }
                });
                menu1.add(menuItem2);
            }
            menuBar1.add(menu1);

            //======== menu3 ========
            {
                menu3.setText("About");

                //---- menuItem5 ----
                menuItem5.setText("Carpeta local");
                menuItem5.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        verRutaLocalActionPerformed(e);
                    }
                });
                menu3.add(menuItem5);

                //---- menuItem6 ----
                menuItem6.setText("Tmin");
                menuItem6.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tminActionPerformed(e);
                    }
                });
                menu3.add(menuItem6);

                //---- menuItem1 ----
                menuItem1.setText("Informaci\u00f3n");
                menuItem1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        infoActionPerformed(e);
                    }
                });
                menu3.add(menuItem1);
            }
            menuBar1.add(menu3);
        }
        setJMenuBar(menuBar1);

        //======== panel1 ========
        {

            // JFormDesigner evaluation mark
            /*panel1.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                    "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                    java.awt.Color.red), panel1.getBorder())); panel1.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});*/

            panel1.setLayout(new GridLayout(4, 0));

            //---- button1 ----
            button1.setText("Subir fichero");
            button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    subirFicheroActionPerformed(e);
                }
            });
            panel1.add(button1);

            //---- button2 ----
            button2.setText("Descargar fichero");
            button2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    descargarFicheroActionPerformed(e);
                }
            });
            panel1.add(button2);

            //---- button4 ----
            button4.setText("Sincronizar reloj");
            button4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sincronizaRelojActionPerformed(e);
                }
            });
            panel1.add(button4);

            //---- button3 ----
            button3.setText("Actualizar directorios");
            button3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actualizarDirectoriosActionPerformed(e);
                }
            });
            panel1.add(button3);
        }
        contentPane.add(panel1, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - juan navarro
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenu menu2;
    private JMenuItem menuItem4;
    private JMenuItem menuItem3;
    private JMenuItem menuItem7;
    private JMenuItem menuItem2;
    private JMenu menu3;
    private JMenuItem menuItem5;
    private JMenuItem menuItem6;
    private JMenuItem menuItem1;
    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private JButton button4;
    private JButton button3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
