package src;
import server.ChatServer;

public class Main {

    public static void main(String[] args) {

        int port = 4027;

        // Try ports until available
        while (true) {
            try {
                ChatServer server = new ChatServer(port);
                server.start();
                break;
            } catch (Exception e) {
            	//if the port is unavailable or there are any problems, it will print a message
            	//and try next port.
                System.out.println("[MAIN] Port " + port + " in use, trying next port...");
                port++;
            }
        }
    }
}