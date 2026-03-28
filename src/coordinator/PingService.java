package coordinator;

import client.ChatClient;

/**
 * PingService periodically sends ping requests to the server.
 * It is used to demonstrate liveness and fault-tolerance support.
 */
public class PingService {
	//client object used to send the ping requests.
    private final ChatClient client;
    //delay between PINGS in milliseconds.
    private final long intervalMillis;
    //controls whether the PING thread should keep running.
    private volatile boolean running = true;
    
    //constructor for PingService.
    public PingService(ChatClient client, long intervalMillis) {
        this.client = client;
        this.intervalMillis = intervalMillis;
    }
    //start the background ping threat.
    public void start() {
    	//create a new threat dedicated to periodic pinging.
        Thread pingThread = new Thread(() -> {
            while (running) {
                try {
                	//wait for the configured interval.
                    Thread.sleep(intervalMillis);
                    //ask the client to send a PING to the server. 
                    client.sendPing();
                } catch (InterruptedException e) {
                	//restore interrupt flag and stop the threat cleanly.
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                	//print any PING-related error.
                    System.out.println("[PING] Error while sending ping: " + e.getMessage());
                }
            }
        });
        //mark threat as daemon so it does not prevent program exit.
        pingThread.setDaemon(true);
        //start the ping thread.
        pingThread.start();
    }
    //stops the ping loop.
    public void stop() {
        running = false;
    }
}