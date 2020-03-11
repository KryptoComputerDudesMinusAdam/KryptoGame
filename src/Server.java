import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {
    public static HashMap<Integer, String> contacts = new HashMap<>();
    public static HashMap<String, String> connections = new HashMap<>();
    public static List<ServerThread> clients = new ArrayList<>();

    public static void main(String[] args) {
        //Scanner scan = new Scanner(System.in);
        //System.out.println("Enter port: ");
        //int port = scan.nextInt();

        // setup server
        int port = 5555;
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                // wait for connections
                System.out.println("Server is open for connections...");
                Socket socket = serverSocket.accept();
                System.out.println("Connection received from port: "+socket.getPort());

                // create a client thread and add client thread into list of clients
                ServerThread client = new ServerThread(socket);
                client.start();
                clients.add(client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

// every ServerThread represents a unique Client connected to the Server
class ServerThread extends Thread {
    Socket socket;
    String clientName;
    int clientPort;
    String receiver;
    ServerThread receiverThread;
    Queue<Message> incomingMessages = new LinkedList<>(); // messages that another client is trying to send to this client

    public ServerThread(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try{
            // for sending messages
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            // for receiving messages
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            // ask for name and set client port
            while(true){
                objectOutputStream.writeObject(new Message("What is your name?"));
                Message m = (Message) objectInputStream.readObject();
                if(!Server.contacts.containsValue(m.encryptedMessage)){
                    this.clientName = m.encryptedMessage;
                    this.clientPort = socket.getPort();
                    Server.contacts.put(this.clientPort, this.clientName);
                    break;
                }
            }
            System.out.println("Client name: "+this.clientName);
            System.out.println("Client port: "+this.clientPort);

            // "empty chat room" until client list is 2 or more
            while(Server.contacts.size() < 2){
                objectOutputStream.writeObject(new Message("waiting for other clients ..."));
                TimeUnit.SECONDS.sleep(5);
            }
            objectOutputStream.writeObject(new Message("Contacts list\n"+Server.contacts.toString()));

            // ask client to pick another client to connect to
            objectOutputStream.writeObject(new Message("Who do you want to talk to?"));
            while(true){
                Message m = (Message) objectInputStream.readObject();
                if(Server.contacts.containsValue(m.encryptedMessage)){
                    this.receiver = m.encryptedMessage;
                    break;
                } else{
                    System.out.println(m.encryptedMessage +"\ndoes not exist in\n"+Server.contacts.toString());
                }
            }

            // wait for receiver to also want to connect to current client
            System.out.println(this.clientName+" "+this.clientPort+" wants to connect to "+this.receiver);
            // TODO: need to implement a waiting connection, maybe use time increment to refresh connection
            objectOutputStream.writeObject(new Message("SUCCESS"));

            // add to connections
            // TODO: this is a weird way of keeping track of connections, maybe we can find something better
            Server.connections.put(this.clientName, this.receiver);
            Server.connections.put(this.receiver, this.clientName);
            for(ServerThread st : Server.clients){
                if(st.clientName.toLowerCase().equals(this.receiver)){
                    receiverThread = st;
                }
            }

            // client can now receive and send messages from their receiver (other client)
            while(true){

                // send client any incoming messages from other client
                if(incomingMessages.peek() != null){
                    objectOutputStream.writeObject(incomingMessages.peek());
                    incomingMessages.remove();
                }

                // receive messages from client
                Message m = (Message) objectInputStream.readObject();
                if(m.cypherOption != -1){ // message is intended for other client, send it to other client
                    System.out.println(m.encryptedMessage);
                    System.out.println("Sending to: "+receiverThread.clientName);
                    receiverThread.incomingMessages.add(m);
                } else{ // message is intended for server, just print it
                    System.out.println(m.encryptedMessage);
                }
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}


/*
TODO: Found a way that maybe Attacker will work and how it can actually be a game
    - Attacker will receive a message every time it queries server
    - Attacker has a list of tools to use at disposal to decrypt the encrypted message
    - If Attacker uses a wrong tool, minus 1 point
    - If Attacker uses a right tool, plus 2 points
    - If Attacker reaches 12 points, Server must shut down because Attacker is too good
        - Attacker wins game
    - If Attacker reaches -6 points, Server closes Attacker socket
        - Attacker loses game
 */