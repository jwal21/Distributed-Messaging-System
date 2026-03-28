package server;

import java.net.ServerSocket;
import java.net.Socket;

import coordinator.CoordinatorService;

public class ChatServer {

    private final int port;
    private final CoordinatorService coordinatorService;

    public ChatServer(int port) {
        this.port = port;
        this.coordinatorService = new CoordinatorService();
    }

    //starts background thread to monitor member heartbeats and remove timed-out members.
    private void startFailureMonitor() {
        Thread monitor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);                                               //check every 5 seconds for timed-out members.
                    coordinatorService.removeTimedOutMembers(10000);   //timeout threshold of 10 seconds.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }


    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

        	//confirms that the server port is listening.
            System.out.println("[SERVER] Listening on port " + port);
            
            //start the failure monitor thread.
            startFailureMonitor();
            
            //infinite loop whilst accepting new clients. 
            while (true) {
            	//wait until a client connects.
                Socket socket = serverSocket.accept();
                System.out.println("[SERVER] Client connected: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, coordinatorService);
                //accepts new clients at the same time.
                new Thread(handler).start();
            }

        } catch (Exception e) {
        	//if the server crashes for whatever reason, it will print a message.
            System.out.println("[SERVER] Error: " + e.getMessage());
        }
    }
}