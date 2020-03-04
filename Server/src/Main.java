import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try{
//            String clientSentence;
//            String capitalizedSentence;
//            ServerSocket welcomeSocket = new ServerSocket(6789);
//
//            while(true) {
//                Socket connectionSocket = welcomeSocket.accept();
//                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//                OutputStream output = connectionSocket.getOutputStream();
//                DataOutputStream outToClient = new DataOutputStream(output);
//                clientSentence = inFromClient.readLine();
//                capitalizedSentence = clientSentence.toUpperCase() + '\n';
//                outToClient.writeBytes(capitalizedSentence);
//            }
            if (args.length < 1) return;

            int port = Integer.parseInt(args[0]);

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
