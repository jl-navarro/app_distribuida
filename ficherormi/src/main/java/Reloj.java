
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

/**
 * Define un reloj.
 * @author Juan Luis Navarro Rey
 * @version 1.0
 */
public class Reloj implements Serializable{
    private long time = 0;
    private Timer timer;

    /**
     * Constructor de la clase. Inicializa el reloj.
     */
    public Reloj(){
        time = System.currentTimeMillis();
        timer = new Timer(1,new TimerEvent());
        timer.start();
    }

    /**
     * Averigua la hora.
     * @return Devuelve la hora en milisegundos.
     */
    public long getTime(){
        return time;
    }

    /**
     * Modifica la hora.
     * @param hora Nueva hora del reloj en milisegundos.
     */
    public void setTime(long hora){
        time = hora;
    }

    /*
     * Clase que define la funcionalidad del timer.
     */
    private class TimerEvent implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            time ++;
        }
    }
}
