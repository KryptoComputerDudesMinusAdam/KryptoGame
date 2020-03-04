import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // for sending messages
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // send welcome message & rules
            dataOutputStream.writeUTF(
                    "WELCOME\n" +
                            "You have successfully connected to the server\n" +
                            "For your first message, respond with your name so we can add you into our contacts list\n" +
                            "Send message 'exit' to terminate connection\n" +
                            "Below you will find the following contacts list: "+"\n");
            dataOutputStream.flush();

            // for receiving messages
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // read the message from the socket
            List<String> messages = new ArrayList<>();
            while(socket.isConnected()){
                String message = dataInputStream.readUTF();
                messages.add(message);

                if(messages.size() == 1){
                    System.out.println("Received name "+ messages.get(0) + ", adding to hash table");
                    // TODO: add port number and name to hash table
                } else if(message.toLowerCase().equals("exit")){
                    System.out.println(messages.get(0) + " requests to close socket");
                    dataOutputStream.writeUTF(messages.get(0) + ", goodbye.");
                    dataOutputStream.flush();
                    socket.close();
                } else {
                    System.out.println(messages.get(0) + " said: " + message);
                }
            }
            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
