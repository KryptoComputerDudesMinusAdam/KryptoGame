import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try{
            // create port number
            Scanner scan = new Scanner(System.in);
            System.out.println("Please input the port number you want your server to run on: ");
            int port = scan.nextInt();

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                // wait for connection
                System.out.println("ServerSocket awaiting connections...");
                while(true){
                    Socket socket = serverSocket.accept();
                    new ServerThread(socket).start();
                    System.out.println("Connection from port: " + socket.getPort() + " hostname: " + socket.getInetAddress().getHostName());
                }
            } catch (EOFException ex) {
                System.out.println("EOF exception: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

// who do you want to talk to, by port number
// every time a new server thread we add it to hash map by port number and name pair
// every time client connects, we see a list of users to connect to