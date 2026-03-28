package client;

import coordinator.PingService;

public class ClientMain {
	//main method for the client.
    public static void main(String[] args) {
    	//ensure the correct number of CLI arguments is provided. 
        if (args.length < 3) {
            System.out.println("[CLIENT] Usage: ClientMain <serverPort> <messageDelayMs> <leaveDelayMs>");
            return;
        }
        //parse the server port.
        int port = Integer.parseInt(args[0]);
        //delay before messaging starts.
        int messageDelay = Integer.parseInt(args[1]);
        //create the ChatClient pointing to localhost on the chosen port.
        int leaveDelay = Integer.parseInt(args[2]);

        ChatClient client = new ChatClient("localhost", port);

        try {
            client.join();

            //start periodic pinging.
            PingService pingService = new PingService(client, 5000);
            pingService.start();

            //first view of the group.
            client.requestMemberList();

            //simulated thinking / typing delay.
            Thread.sleep(messageDelay);

            //refresh group state before choosing a message target.
            client.requestMemberList();

            //private message only if another client is actually present.
            if (client.hasAnotherMemberAvailable()) {
                client.sendPrivateMessageToFirstOtherMember("Hi, this is " + client.getClientId());
                Thread.sleep(1200);
            }

            //broadcast to all other members.
            client.sendBroadcastMessage("Hello everyone, this is " + client.getClientId());
            Thread.sleep(2500);

            //read delivered messages.
            client.requestInbox();

            //stay in the system long enough for coordinator handover demo.
            Thread.sleep(leaveDelay);

            pingService.stop();
            //leave the group.
            client.leave();

        } catch (Exception e) {
        	//print any client-side error.
            System.out.println("[CLIENT] Error: " + e.getMessage());
        }
    }
}