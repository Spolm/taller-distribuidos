/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taller1sd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.json.JSONObject;

/**
 *
 * @author franciscogomezlopez
 */
public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private HashMap<String, String> participant = new HashMap<>();
    private boolean finishGame = false;
    private boolean tengopapa = false;
    private String name = "";
    private boolean sendFinishGame = false;

    public void start(int port, String name) throws IOException {

        System.out.println("Im listening ... on " + port + " I'm " + name);
        this.name = name;

        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String greeting = in.readLine();
        if (greeting.startsWith("regparticipante")) {
            //guardarEstado(greeting);
            int totallength = "regparticipante".length();

            String partipante = greeting.substring(totallength, totallength + 4);
            String puerto = greeting.substring(totallength + 4, totallength + 8);
            String ip = greeting.substring(totallength + 8);

            // agregar participante
            this.participant.put(partipante, puerto + ":" + ip);

            // paseo por cada uno de los elementos de los participantes para enviar la lista
            for (Map.Entry<String, String> entry : participant.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!key.equals(this.name)) {

                    ClientMessage sendmessage = new ClientMessage();
                    sendmessage.startConnection(value.substring(5), new Integer(value.substring(0, 4)));
                    sendmessage.sendMessage("actualizalista" + this.serializarLista());
                    
                }

            }

            out.println("agregadoparticipante");

        } else if (greeting.startsWith("recibepapa")) {

            this.tengopapa = true;
            guardarEstado(true);
            System.out.println("TEngo la papa " + this.name);
            //guardarEstado("tengo la papa" + this.name);
            out.println("tienes la papa" + this.name);
            // agregar participante

            if (this.finishGame && this.tengopapa) {

                System.out.println("perdiste:" + this.name);
                System.exit(0);
            }

        } else if (greeting.startsWith("sequemo")) {

            this.finishGame = true;

            // agregar participante
            if (this.finishGame && this.tengopapa) {

                System.out.println("perdiste:" + this.name);
                System.exit(0);
            }

            if (!this.sendFinishGame) {
                for (Map.Entry<String, String> entry : participant.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if (!key.equals(this.name)) {
                        SendMessageThread hilo = new SendMessageThread();
                        hilo.setSeconds(0);
                        hilo.setValue(value);
                        hilo.setMessage("sequemo");
                        (new Thread(hilo)).start();
                    }

                }
                this.sendFinishGame = true;
            }
            out.println("finish se quemo");

        } else if (greeting.startsWith("actualizalista")) {
            
            String lista = greeting.substring("actualizalista".length());

            String[] listatmp = lista.split(",");
            this.participant = new HashMap<String, String>();
            for (String tmp : listatmp) {
                String[] finaltmp = tmp.split("#");
                this.participant.put(finaltmp[0], finaltmp[1]);
                //guardarEstado(tmp);
            }

        } else {
            System.out.println("Mensaje no reconocido");
            out.println("mensaje corrupto vete de aqui");
        }

        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();

        if (this.tengopapa) {
            try {
                this.sendingPapa();
            } catch (InterruptedException ex) {
                System.out.println("Error sending papa");
                //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        guardarEstado();
        this.start(port, name);

    }

    private void sendingPapa() throws InterruptedException, IOException {

        boolean found = false;

        for (Map.Entry<String, String> entry : participant.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals(this.name)) {
                found = true;
            } else {

                if (found) {
                    // siguiente
//                    ClientMessage sendmessage = new ClientMessage();
//                    sendmessage.startConnection(value.substring(5), new Integer(value.substring(0, 4)));
//                    sendmessage.sendMessage("recibepapa");

                    SendMessageThread hilo = new SendMessageThread();
                    hilo.setValue(value);
                    hilo.setMessage("recibepapa");
                    hilo.setRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Server.this.guardarEstado(true);
                            }
                            catch(Exception e) {
                                System.out.println("fallo");
                            }
                        }
                    });
                    (new Thread(hilo)).start();
                    System.out.println("bye hilo1");
                    this.tengopapa = false;
                    break;
                }
            }

        }

        if (this.tengopapa) {
            Map.Entry<String, String> entry = this.participant.entrySet().iterator().next();
            String key = entry.getKey();
            String value = entry.getValue();

            SendMessageThread hilo = new SendMessageThread();
            hilo.setValue(value);
            hilo.setMessage("recibepapa");
            (new Thread(hilo)).start();
            System.out.println("bye hilo2");
            this.tengopapa = false;
        }

    }

    private String serializarLista() {

        String finallista = "";

        for (Map.Entry<String, String> entry : participant.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            finallista += key + "#" + value + ",";
        }

        return finallista;
    }

    
    public void guardarEstado(boolean papa){
        File f;
        f = new File(this.name+".txt");
        
        try{
            FileWriter w = new FileWriter(f);
            PrintWriter pw = new PrintWriter(w);
            pw.println(serializarLista());
            pw.println("papa: "+ papa);
            pw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void guardarEstado(){
        File f;
        f = new File(this.name+".txt");
        
        try{
            FileWriter w = new FileWriter(f);
            PrintWriter pw = new PrintWriter(w);
            pw.println(serializarLista());
            pw.println("papa: "+ tengopapa);
            pw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public boolean leerEstado(){
        try{
            File file = new File(this.name +".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            int i = 0;
            while ((st = br.readLine()) != null){
                System.out.println(st);
                if(i == 0){
                    String[] listatmp = st.split(",");
                    this.participant = new HashMap<String, String>();
                for (String tmp : listatmp) {
                    String[] finaltmp = tmp.split("#");
                    this.participant.put(finaltmp[0], finaltmp[1]);
                }    
            }
            else{
                if (
                        st.equals("papa:false")) {
                    this.tengopapa = false;
                }
                else{
                    this.tengopapa = true;
                }
            }
            i++;
        }
        } catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
    
  
    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}
