package Chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 * La clase HiloCliente representa un hilo que gestiona la comunicación con un cliente.
 */

public class HiloCliente extends Thread{
    
	//Socket para escuchar al cliente
    private final Socket socket;    
    
    //Objeto para realizar envios al servidor
    private ObjectOutputStream objectOutputStream;
    //Objeto para recibir envios del servidor
    private ObjectInputStream objectInputStream;            
    
    //Servidor del hilo en ejecucion
    private final Servidor server;
    
    //Identificador del hilo en ejecucion
    private String idHilo;

    //Control de esucha del servidor
    private boolean escuchando;
   
    /**
    * Método constructor de la clase hilo cliente.
    * @param socket
    * @param server 
    */
    public HiloCliente(Socket socket,Servidor server) {
        this.server=server;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("Error en la inicialización del ObjectOutputStream y el ObjectInputStream");
        }
    }
    
    
   //Metodo de cierre de la conexion   
    public void desconnectar() {
        try {
            socket.close();
            escuchando=false;
        } catch (IOException ex) {
            System.err.println("Error al cerrar el socket de comunicación con el cliente.");
        }
    }
    
    //Ejecucion del ciclo de escucha      
    public void run() {
        try{
            escuchar();
        } catch (Exception ex) {
            System.err.println("Error al llamar al método readLine del hilo del cliente.");
        }
        desconnectar();
    }
        
    //Metodo de escucha ejecutado por el metodo run      
    public void escuchar(){        
        escuchando=true;
        while(escuchando){
            try {
                Object aux=objectInputStream.readObject();
                if(aux instanceof LinkedList){
                    ejecutar((LinkedList<String>)aux); 
                }
            } catch (Exception e) {                    
                System.err.println("Error al leer lo enviado por el cliente.");
            }
        }
    }
    
    
    //Control de acciones realizadas en la aplicacion   
    public void ejecutar(LinkedList<String> lista){
        // 0 - El primer elemento de la lista es siempre el tipo
        String tipo=lista.get(0);
        switch (tipo) {
            case "SOLICITUD_CONEXION":
                // 1 - Identificador propio del nuevo usuario
                confirmarConexion(lista.get(1));
                break;
            case "SOLICITUD_DESCONEXION":
                // 1 - Identificador propio del nuevo usuario
                confirmarDesConexion();
                break;                
            case "MENSAJE":
                // 1      - Cliente emisor
                // 2      - Cliente receptor
                // 3      - Mensaje
                String destinatario=lista.get(2);
                server.clientes
                        .stream()
                        .filter(h -> (destinatario.equals(h.getIdentificador())))
                        .forEach((h) -> h.enviarMensaje(lista));
                break;
            default:
                break;
        }
    }


    //Metodo de envio de informacion al cliente
    private void enviarMensaje(LinkedList<String> lista){
        try {
            objectOutputStream.writeObject(lista);            
        } catch (Exception e) {
            System.err.println("Error al enviar el objeto al cliente.");
        }
    }    
    
    //Metodo de confirmacion de conexion permite a los usuarios saber que hay un nuevo usuario conectado
    private void confirmarConexion(String identificador) {
        Servidor.identificadorCliente++;
        this.idHilo=Servidor.identificadorCliente+" - "+identificador;
        LinkedList<String> lista=new LinkedList<>();
        lista.add("CONEXION_ACEPTADA");
        lista.add(this.idHilo);
        lista.addAll(server.getUsuariosConectados());
        enviarMensaje(lista);
        server.agregarLog("\nNuevo cliente: "+this.idHilo);
        //enviar a todos los clientes el nombre del nuevo usuario conectado excepto a él mismo
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("NUEVO_USUARIO_CONECTADO");
        auxLista.add(this.idHilo);
        server.clientes
                .stream()
                .forEach(cliente -> cliente.enviarMensaje(auxLista));
        server.clientes.add(this);
    }
    
    
    //Metodo que devuelve el id unico por cada cliente conectado
    public String getIdentificador() {
        return idHilo;
    }
    
    //Metodo de confirmacion de desconexion de un usuario permite a los usuarios saber que un usuario se ha desconectado
    private void confirmarDesConexion() {
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("USUARIO_DESCONECTADO");
        auxLista.add(this.idHilo);
        server.agregarLog("\nEl cliente \""+this.idHilo+"\" se ha desconectado.");
        this.desconnectar();
        for(int i=0;i<server.clientes.size();i++){
            if(server.clientes.get(i).equals(this)){
                server.clientes.remove(i);
                break;
            }
        }
        server.clientes
                .stream()
                .forEach(h -> h.enviarMensaje(auxLista));        
    }
}