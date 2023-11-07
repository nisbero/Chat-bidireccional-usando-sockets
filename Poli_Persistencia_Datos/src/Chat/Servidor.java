package Chat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 * La clase Servidor representa un servidor de chat que gestiona la comunicación con múltiples clientes.
 */

public class Servidor extends Thread{    
  
	//Socket de escucha del servidor para evaluar los clientes que se conectan
    private ServerSocket sSocket;
   
    //Hilos de conexion de cada cliente
    LinkedList<HiloCliente> clientes;
    
    //Interface grafica del servidor
    private final EjecutableServidor ventana;

    //Puerto de conexion
    private final String puerto;
    
    //Identificador unico por cada cliente que se conecta
    static int identificadorCliente;
    
    /**
     * Constructor.
     * @param puerto
     * @param ventana 
     */
    public Servidor(String puerto, EjecutableServidor ventana) {
    	identificadorCliente=0;
        this.puerto=puerto;
        this.ventana=ventana;
        clientes=new LinkedList<>();
        this.start();
    }
    
    //Metodo de escucha del servidor 
    public void run() {
        try {
        	sSocket = new ServerSocket(Integer.valueOf(puerto));
            ventana.addServidorIniciado();
            while (true) {
                HiloCliente h;
                Socket socket;
                socket = sSocket.accept();
                System.out.println("Nueva conexion entrante: "+socket);
                h=new HiloCliente(socket, this);               
                h.start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ventana, "Error al iniciar el servidor,\n"
                                                 + "por favor verifique los datos ingresados.\n"
                                                 + "el aplicativo se cerrara.");
            System.exit(0);
        }                
    }        
    
    //Log de clientes conectados
    LinkedList<String> getUsuariosConectados() {
        LinkedList<String>usuariosConectados=new LinkedList<>();
        clientes.stream().forEach(c -> usuariosConectados.add(c.getIdentificador()));
        return usuariosConectados;
    }
    
    
    //Añade una linea al log
    void agregarLog(String texto) {
        ventana.agregarLog(texto);
    }
}
