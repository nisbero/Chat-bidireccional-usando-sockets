package Chat;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

import Chat.EjecutableCliente;


/**
 * La clase Cliente representa un cliente de chat que se conecta a un servidor.
 */

public class Cliente  extends Thread {
    
	//Socket de conexion
    private Socket socket;
   
    //Objetos para enviar datos al servidor
    private ObjectOutputStream objectOutputStream;
    //Objetos para recibir los datos del servidor   
    private ObjectInputStream objectInputStream;
   
    //Interface grafica      
    private final EjecutableCliente ventana;    

    //id del cliente
    private String idCliente;
   
    //validador de encendido del sistema de escucha
    private boolean escuchando;
    
    //variable para guardar la ip de conexion
    private final String ip;
    
    ///Puerto de conexion
    private final int puerto;
    
    /**
     * Constructor de la clase cliente.
     * @param ventana
     * @param host
     * @param puerto
     * @param nombre 
     */
    Cliente(EjecutableCliente ventana, String host, Integer puerto, String nombre) {
        this.ventana=ventana;        
        this.ip=host;
        this.puerto=puerto;
        this.idCliente=nombre;
        escuchando=true;
        this.start();
    }
    
    //Metodo que ejecuta la conexion del lado del cliente
    public void run(){
        try {
            socket=new Socket(ip, puerto);
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectInputStream=new ObjectInputStream(socket.getInputStream());
            System.out.println("Conexion exitosa!!!!");
            this.enviarSolicitudConexion(idCliente);
            this.escuchar();
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(ventana, "Conexión fallida, servidor desconocido,\n"
                                                 + "por favor valide los datos ingresados\n"
                                                 + "o el servidor caido.\n"
                                                 + "La aplicacion se cerrara");
            System.exit(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventana, "Conexión fallida, error de Entrada/Salida,\n"
                                                 + "por favor valide los datos ingresados\n"
                                                 + "o el servidor caido.\n"
                                                 + "La aplicacion se cerrara");
            System.exit(0);
        }

    }
    
    //Metodo para ejecutar la desconexion del cliente
    public void desconectar(){
        try {
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();  
            escuchando=false;
        } catch (Exception e) {
            System.err.println("Fallo al cerrar la comunicación del cliente.");
        }
    }
    
    //Metodo de envio de mensajes al servidor
    public void enviarMensaje(String cliente_receptor, String mensaje){
        LinkedList<String> lista=new LinkedList<>();
        
        //tipo       
        lista.add("MENSAJE");        
        //cliente emisor
        lista.add(idCliente);
        //cliente receptor
        lista.add(cliente_receptor);
        //mensaje que se desea transmitir
        lista.add(mensaje);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje al servidor.");
        }
    }
    
    //Metodo para escuchar al servidor
    public void escuchar() {
        try {
            while (escuchando) {
                Object aux = objectInputStream.readObject();
                if (aux != null) {
                    if (aux instanceof LinkedList) {
                        //Si se recibe una LinkedList entonces se procesa
                        ejecutar((LinkedList<String>)aux);
                    } else {
                        System.err.println("Se recibio un objeto que genero un fallo");
                    }
                } else {
                    System.err.println("No hay respuesta por parte del servidor");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ventana, "Conexion con el servidor caida\n"
                                                 + "el chat debe terminar.\n"
                                                 + "LA aplicación se cerrara.");
            System.exit(0);
        }
    }
    
    //Acciones que ejecutara la aplicacion
    public void ejecutar(LinkedList<String> lista){
    	
        // 0 - El primer elemento de la lista es siempre el tipo
        String tipo=lista.get(0);    
        switch (tipo) {
            case "CONEXION_ACEPTADA":
                // 1      - Identificador propio del nuevo usuario
                // 2 .. n - Identificadores de los clientes conectados actualmente
            	idCliente=lista.get(1);
                ventana.sesionIniciada(idCliente);
                for(int i=2;i<lista.size();i++){
                    ventana.addContacto(lista.get(i));
                }
                break;
            case "NUEVO_USUARIO_CONECTADO":
                // 1      - Identificador propio del cliente que se acaba de conectar
                ventana.addContacto(lista.get(1));
                break;
            case "USUARIO_DESCONECTADO":
                // 1      - Identificador propio del cliente que se acaba de conectar
                ventana.eliminarContacto(lista.get(1));
                break;                
            case "MENSAJE":
                // 1      - Cliente emisor
                // 2      - Cliente receptor
                // 3      - Mensaje  
            	ventana.addMensaje(lista.get(1), lista.get(3));               
                break;
            default:
                break;
        }
    }
    
    //Solicitud de conexion al servidor
    private void enviarSolicitudConexion(String identificador) {
        LinkedList<String> lista=new LinkedList<>();
        //Tipo de accion a realizar
        lista.add("SOLICITUD_CONEXION");
        //Id del cliente solicitante
        lista.add(identificador);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Fallo al conectar con el servidor");
        }
    }
    
    //Confirmacion de desconexion por parte del usuario
    void confirmarDesconexion() {
        LinkedList<String> lista=new LinkedList<>();
        //tipo
        lista.add("SOLICITUD_DESCONEXION");
        //cliente solicitante
        lista.add(idCliente);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Fallo al conectar con el servidor.");
        }
    }
    
    //Captura del identificador del cliente
    String getIdentificador() {
        return idCliente;
    }
}