import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) {
        // get port
        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter port: ");
//        int port = scan.nextInt();

        int port = 5555;

        //connect to socket
        try{
            TimeUnit.SECONDS.sleep(4);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        try(Socket socket = new Socket("localhost", port)) {
            ReceiverThread recv = new ReceiverThread(socket);
            SenderThread send = new SenderThread(socket);

            recv.start();
            send.start();

            recv.sender = send;
            send.receiver = recv;


            while(true){
            }

        }catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}

class SenderThread extends Thread {
    private Socket socket; // server
    public ReceiverThread receiver;
    public boolean isSending = false;
    public boolean preparationMode = true;

    public SenderThread(Socket socket){
        this.socket = socket;
    }

    public synchronized void run(){
        try{
            // for sending messages
            Scanner scanStr = new Scanner(System.in);
            Scanner scanInt = new Scanner(System.in);
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            System.out.println("Sender Thread Running");

            String message="";
            while(!message.toLowerCase().equals("exit")){
                TimeUnit.SECONDS.sleep(2);
                if(preparationMode){
                    System.out.print("Message Server: ");
                    message = scanStr.nextLine();
                    isSending = true;
                    //dataOutputStream.writeUTF(message);
                    //dataOutputStream.flush();
                    isSending = false;
                    objectOutputStream.writeObject(new Message(message));
                } else{
                    isSending = true;
                    System.out.print("Message Client: ");
                    message = scanStr.nextLine();
                    System.out.print("\tOption: ");
                    int option = scanInt.nextInt();
                    //dataOutputStream.writeUTF(message);
                    //dataOutputStream.flush();
                    isSending = false;
                    objectOutputStream.writeObject(new Message(message, option));
                }

            }
        } catch(Exception e){
            System.out.println("CAUGHT"+e.getMessage());
        }
    }
}

class ReceiverThread extends Thread {
    private Socket socket;
    public SenderThread sender;

    public ReceiverThread(Socket socket){
        this.socket = socket;
    }

    public synchronized void run() {
        try {
            // for receiving messages
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);


            System.out.println("Receiver Thread Running");
            while (socket.isConnected() && !sender.isSending){
                //String response = dataInputStream.readUTF();
                Message m = (Message) objectInputStream.readObject();
                if(m.e.equals("SUCCESS")){
                    sender.preparationMode = false;
                }
                if(m.option != -1){
                    System.out.println("\nFrom Client: "+m.e);
                } else{
                    System.out.println("\nFrom Server: "+m.e);
                }
            }

        } catch (Exception e){
            System.out.println("SUPER CAUGHT"+e.getMessage());
        }
    }
}