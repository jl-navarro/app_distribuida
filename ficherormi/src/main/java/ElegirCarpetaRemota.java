import java.awt.*;
import java.awt.event.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.rmi.RemoteException;
import javax.swing.*;
/*
 * Created by JFormDesigner on Sun Nov 16 12:12:35 CET 2014
 */



/**
 * Clase auxiliar para elegir carpeta remota.
 * @author Juan Luis Navarro Rey.
 * @version 1.0
 */
public class ElegirCarpetaRemota extends JFrame {

    private Proxy proxy;

    /**
     * Constructor de la clase.
     * @param model Lista con el nombre de las posibles carpetas remotas.
     * @param proxy Interfaz que conecta el cliente con el servidor.
     */
    public ElegirCarpetaRemota(ComboBoxModel<String> model, Proxy proxy) {
        initComponents();
        comboBox1.setModel(model);
        this.proxy = proxy;
    }

    private void operacionErronea(String error){
        JOptionPane.showMessageDialog(null,error,"ERROR",JOptionPane.ERROR_MESSAGE);
    }

    private void salirActionPerformed(ActionEvent e) {
        dispose();
    }

    private void cambiarDirectorioActionPerformed(ActionEvent e) {
        boolean ok = false;
        RespuestaServidor<Boolean>respuesta = null;
        Path path = null;
        if(checkBox1.isSelected()){
            path = FileSystems.getDefault().getPath(textField1.getText());
        }else{
            path = FileSystems.getDefault().getPath(comboBox1.getSelectedItem().toString());
        }
        try {
            respuesta = this.proxy.modificaCarpetaServidor(path.toString());
            ok = respuesta.getValor();
        } catch (RemoteException e1) {
            operacionErronea(e1.toString());
        }
        if(!ok){
            operacionErronea(respuesta.getError());
        }
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Juan Navarro
        panel1 = new JPanel();
        panel3 = new JPanel();
        label1 = new JLabel();
        hSpacer1 = new JPanel(null);
        comboBox1 = new JComboBox();
        panel4 = new JPanel();
        panel6 = new JPanel();
        checkBox1 = new JCheckBox();
        label3 = new JLabel();
        panel5 = new JPanel();
        label2 = new JLabel();
        hSpacer2 = new JPanel(null);
        textField1 = new JTextField();
        panel2 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();

        //======== this ========
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Nueva carpeta remota");
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayout());

        //======== panel1 ========
        {

            // JFormDesigner evaluation mark
            /*panel1.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                    "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                    java.awt.Color.red), panel1.getBorder())); panel1.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});*/

            panel1.setLayout(new GridLayout(3, 1));

            //======== panel3 ========
            {
                panel3.setLayout(new FlowLayout(FlowLayout.LEFT));

                //---- label1 ----
                label1.setText("Elije la carpeta:");
                panel3.add(label1);
                panel3.add(hSpacer1);

                //---- comboBox1 ----
                comboBox1.setModel(new DefaultComboBoxModel(new String[] {
                    "dfsf",
                    "sfsdf",
                    "sfsdf",
                    "sdf"
                }));
                comboBox1.setPreferredSize(new Dimension(200, 25));
                panel3.add(comboBox1);
            }
            panel1.add(panel3);

            //======== panel4 ========
            {
                panel4.setLayout(new GridLayout(2, 2));

                //======== panel6 ========
                {
                    panel6.setLayout(new GridLayout(2, 1));

                    //---- checkBox1 ----
                    checkBox1.setText("A\u00f1adir ruta manualmente");
                    panel6.add(checkBox1);

                    //---- label3 ----
                    label3.setText("Para ello fijese la forma que tiene la ruta en el desplegable de arriba.");
                    panel6.add(label3);
                }
                panel4.add(panel6);

                //======== panel5 ========
                {
                    panel5.setLayout(new FlowLayout(FlowLayout.LEFT));

                    //---- label2 ----
                    label2.setText("Otra ubicaci\u00f3n");
                    panel5.add(label2);
                    panel5.add(hSpacer2);

                    //---- textField1 ----
                    textField1.setText("Escriba la ruta");
                    textField1.setPreferredSize(new Dimension(250, 23));
                    panel5.add(textField1);
                }
                panel4.add(panel5);
            }
            panel1.add(panel4);

            //======== panel2 ========
            {
                panel2.setLayout(new FlowLayout());

                //---- button1 ----
                button1.setText("Aceptar");
                button1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cambiarDirectorioActionPerformed(e);
                    }
                });
                panel2.add(button1);

                //---- button2 ----
                button2.setText("Cancelar");
                button2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        salirActionPerformed(e);
                    }
                });
                panel2.add(button2);
            }
            panel1.add(panel2);
        }
        contentPane.add(panel1);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Juan Navarro
    private JPanel panel1;
    private JPanel panel3;
    private JLabel label1;
    private JPanel hSpacer1;
    private JComboBox comboBox1;
    private JPanel panel4;
    private JPanel panel6;
    private JCheckBox checkBox1;
    private JLabel label3;
    private JPanel panel5;
    private JLabel label2;
    private JPanel hSpacer2;
    private JTextField textField1;
    private JPanel panel2;
    private JButton button1;
    private JButton button2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
