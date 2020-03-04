import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try{
            Scanner scan = new Scanner(System.in);
            System.out.println("Please input the port number you want your server to run on: ");
            int port = scan.nextInt();

            if (args.length < 1) return;

            try (ServerSocket serverSocket = new ServerSocket(port)) {

                System.out.println("Server is listening on port " + port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");

                    new ServerThread(socket).start();
                }

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