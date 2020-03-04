import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try{
            // get port number
            Scanner scanInt = new Scanner(System.in);
            Scanner scanStr = new Scanner(System.in);
            System.out.println("Please enter the port number you want to connect to as a client: ");
            int port = scanInt.nextInt();

            try (Socket socket = new Socket("localhost", port)) {
                // for receiving messages
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                System.out.println(dataInputStream.readUTF());

                // for sending messages
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                // write messages
                boolean writing = true;
                while(writing){
                    System.out.print("Send your message: ");
                    String message = scanStr.nextLine();
                    dataOutputStream.writeUTF(message);
                    dataOutputStream.flush(); // send the message
                    System.out.println();
                    System.out.print("Done sending your messages? (y/n): ");
                    String answer = scanStr.nextLine();
                    if(answer.toLowerCase().equals("y")){
                        writing = false;
                    }
                }
                //dataOutputStream.close();
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        } catch(Exception e){

        }
    }

}
