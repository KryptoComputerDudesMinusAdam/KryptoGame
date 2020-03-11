import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter port: ");
//        int port = scan.nextInt();

        int port = 8888;

        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                Socket socket = serverSocket.accept();
                ServerThread client = new ServerThread(socket);
                client.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class Server {
    public static HashMap<Integer, String> contacts = new HashMap<>();
    public static HashMap<String, String> connections = new HashMap<>();
}

class ServerThread extends Thread {
    Socket socket;
    String clientName;
    int clientPort;
    String receiver;

    public ServerThread(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try{
            // for sending messages
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // for receiving messages
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // ask for name and set client port
            while(true){
                dataOutputStream.writeUTF("What is your name?");
                dataOutputStream.flush();
                String response = dataInputStream.readUTF();
                if(!Server.contacts.containsValue(response)){
                    this.clientName = response;
                    this.clientPort = socket.getPort();
                    Server.contacts.put(this.clientPort, this.clientName);
                    break;
                }
            }
            System.out.println("Client name: "+this.clientName);
            System.out.println("Client port: "+this.clientPort);

            // show contacts
            dataOutputStream.writeUTF("waiting for other clients ...");
            dataOutputStream.flush();
            while(true){
                if(Server.contacts.size() > 1){
                    dataOutputStream.writeUTF("Contacts list\n"+Server.contacts.toString());
                    dataOutputStream.flush();
                    break;
                }
            }

            // ask for receiver
            dataOutputStream.writeUTF("Who do you want to talk to?");
            dataOutputStream.flush();
            while(true){
                String response = dataInputStream.readUTF();
                if(Server.contacts.containsValue(response)){
                    this.receiver = response;
                    break;
                } else{
                    System.out.println(response+"\ndoes not exist in\n"+Server.contacts.toString());
                }
            }

            // add to connections
            Server.connections.put(this.clientName, this.receiver);


            System.out.println(this.clientName+" "+this.clientPort+" wants to connect to "+this.receiver);

            while(true){
                System.out.println(dataInputStream.readUTF());
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
