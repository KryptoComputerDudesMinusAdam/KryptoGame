import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static HashMap<Integer, String> contacts = new HashMap<>();
    public static HashMap<String, String> connections = new HashMap<>();
    public static List<ServerThread> clients = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter port: ");
//        int port = scan.nextInt();

        int port = 9999;

        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                Socket socket = serverSocket.accept();
                ServerThread client = new ServerThread(socket);
                client.start();
                clients.add(client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class ServerThread extends Thread {
    Socket socket;
    String clientName;
    int clientPort;
    String receiver;
    ServerThread receiverThread;
    Queue<Message> incomingMessages = new LinkedList<>();

    public ServerThread(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try{
            // for sending messages
            OutputStream outputStream = socket.getOutputStream();
            //DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            // for receiving messages
            InputStream inputStream = socket.getInputStream();
            //DataInputStream dataInputStream = new DataInputStream(inputStream);

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            // ask for name and set client port
            while(true){
                objectOutputStream.writeObject(new Message("What is your name?"));
                //dataOutputStream.writeUTF("What is your name?");
                //dataOutputStream.flush();
                Message m = (Message) objectInputStream.readObject();
                //String response = dataInputStream.readUTF();
                if(!Server.contacts.containsValue(m.e)){
                    this.clientName = m.e;
                    this.clientPort = socket.getPort();
                    Server.contacts.put(this.clientPort, this.clientName);
                    break;
                }
            }
            System.out.println("Client name: "+this.clientName);
            System.out.println("Client port: "+this.clientPort);

            // show contacts
            objectOutputStream.writeObject(new Message("waiting for other clients ..."));
            //dataOutputStream.writeUTF("waiting for other clients ...");
            //dataOutputStream.flush();
            while(true){
                if(Server.contacts.size() > 1){
                    objectOutputStream.writeObject(new Message("Contacts list\n"+Server.contacts.toString()));
                    //dataOutputStream.writeUTF("Contacts list\n"+Server.contacts.toString());
                    //dataOutputStream.flush();
                    break;
                }
            }

            // ask for receiver
            objectOutputStream.writeObject(new Message("Who do you want to talk to?"));
            //dataOutputStream.writeUTF("Who do you want to talk to?");
            //dataOutputStream.flush();
            while(true){
                Message m = (Message) objectInputStream.readObject();
                //String response = dataInputStream.readUTF();
                if(Server.contacts.containsValue(m.e)){
                    this.receiver = m.e;
                    break;
                } else{
                    System.out.println(m.e+"\ndoes not exist in\n"+Server.contacts.toString());
                }
            }

            // add to connections
            Server.connections.put(this.clientName, this.receiver);
            Server.connections.put(this.receiver, this.clientName);
            for(ServerThread st : Server.clients){
                if(st.clientName.toLowerCase().equals(this.receiver)){
                    receiverThread = st;
                }
            }


            System.out.println(this.clientName+" "+this.clientPort+" wants to connect to "+this.receiver);
            objectOutputStream.writeObject(new Message("SUCCESS"));
            //dataOutputStream.writeUTF("SUCCESS");
            while(true){
                //System.out.println(dataInputStream.readUTF());
                if(incomingMessages.peek() != null){
                    objectOutputStream.writeObject(incomingMessages.peek());
                    incomingMessages.remove();
                }

                Message m = (Message) objectInputStream.readObject();
                if(m.option != -1){
                    System.out.println(m.e);
                    System.out.println("Sending to: "+receiverThread.clientName);
                    receiverThread.incomingMessages.add(m);
                } else{
                    System.out.println(m.e);
                }
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
