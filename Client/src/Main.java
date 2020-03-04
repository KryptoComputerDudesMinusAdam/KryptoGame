import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try{
            Scanner scanInt = new Scanner(System.in);
            Scanner scanStr = new Scanner(System.in);
            System.out.println("Please enter the port number you want to connect to as a client: ");
            int port = scanInt.nextInt();
            System.out.println("Please enter the port number you want to connect to as a client: ");
            String hostname = scanStr.nextLine();
            if (args.length < 2) return;

            try (Socket socket = new Socket(hostname, port)) {

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                Console console = System.console();
                String text;

                do {
                    text = console.readLine("Enter text: ");

                    writer.println(text);

                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    String time = reader.readLine();

                    System.out.println(time);

                } while (!text.equals("bye"));

                //socket.close();

            } catch (UnknownHostException ex) {

                System.out.println("Server not found: " + ex.getMessage());

            } catch (IOException ex) {

                System.out.println("I/O error: " + ex.getMessage());
            }
        } catch(Exception e){

        }
    }
}
