/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taller1sd;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author franciscogomezlopez
 */
public class Taller1sd {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        if(args[0].startsWith("server")) {
            try {
                
                Server serer = new Server();
                serer.start(new Integer(args[1]), args[2]);
                
            } catch (IOException ex) {
                System.out.println("error server");
                //Logger.getLogger(Taller1sd.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            try {
                //participante
                
                ClientMessage message = new ClientMessage();
                message.startConnection(args[1], new Integer(args[2]));
                
                String response = message.sendMessage(args[3]);
                
                System.out.println("Respuesta server: " + response);
                
            } catch (IOException ex) {
                System.out.println("error client message");
                Logger.getLogger(Taller1sd.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        
        }
        
        
    }
    
}
