import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

/**
 * Clase principal del cliente.
 * @author Juan Luis Navarro Rey.
 * @version 1.0
 */
public class Cliente{

    // -Djava.rmi.server.codebase=file:///home/juan/IdeaProjects/ficherormi/target/classes -Djava.security.policy=file:///home/juan/IdeaProjects/ficherormi/target/classes/server.policy

    public static void main(String[]args) throws RemoteException, NotBoundException, MalformedURLException {
        String ip = JOptionPane.showInputDialog(null,"Escriba la ip del servidor (en blanco: localhost)","IP del servidor",JOptionPane.QUESTION_MESSAGE);
        if((ip == null)||(ip.equals(""))){
            ip = "localhost";
        }
        System.setSecurityManager(new RMISecurityManager());
        Proxy p = (Proxy) Naming.lookup("rmi://" +ip +":2005/Proxy");
        new GuiCliente(p);
    }
}
