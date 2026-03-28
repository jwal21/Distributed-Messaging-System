package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Message;
import model.MessageType;

public class ChatClient {
	//server host to connect to, usually localhost in local testing. 
    private final String serverHost;
    //server port to connect to.
    private final int serverPort;
    //unique client ID used to identify the client in the system. 
    private final String clientId;

    private String lastMemberList = "";

    public ChatClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientId = "client-" + System.currentTimeMillis();
    }

    public String getClientId() {
        return clientId;
    }

    public void join() throws Exception {
    	//open a socket connection to the server.
        Socket socket = new Socket(serverHost, serverPort);
        //create output stream first to match server order. 
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        //create input stream to read the server's reply.
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //build the JOIN request message.
        Message join = new Message(
                MessageType.JOIN,
                clientId,
                "Joining group"
        );
        //send the JOIN request message.
        out.writeObject(join);
        out.flush();
        //print local log.
        System.out.println("[CLIENT] Sent JOIN as " + clientId);
        //read the server response.
        Message response = (Message) in.readObject();
        //if response is WELCOME, print the coordinator info.
        if (response.getType() == MessageType.WELCOME) {
            System.out.println("[CLIENT] Welcome received. Coordinator is: " + response.getContent());
        }
        //close the socket after the request/response cycle is complete. 
        socket.close();
    }

    public void requestMemberList() throws Exception {
        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //build the request message.
        Message request = new Message(
                MessageType.MEMBER_LIST_REQUEST,
                clientId,
                "Requesting member list"
        );
        //send request.
        out.writeObject(request);
        out.flush();
        //log request locally. 
        System.out.println("[CLIENT] Sent MEMBER_LIST_REQUEST as " + clientId);

        Message response = (Message) in.readObject();
        //if the response is correct, store it and print it. 
        if (response.getType() == MessageType.MEMBER_LIST_RESPONSE) {
            lastMemberList = response.getContent();
            System.out.println("[CLIENT] Member list received:");
            System.out.println(lastMemberList);
        }

        socket.close();
    }
    //parses the most recently received member list and returns
    //the first active member ID that is not this client.
    private String getFirstOtherMemberIdFromLastMemberList() {
    	//split member list into lines. 
        String[] lines = lastMemberList.split("\\n");
        //process each line
        for (String line : lines) {
            line = line.trim();
            //member entries begin with "-ID"
            if (line.startsWith("- ID: ")) {
            	//remove prefix.
                String withoutPrefix = line.substring(6);
                String[] parts = withoutPrefix.split(",");
                //first field contains the member ID.
                String memberId = parts[0].trim();

                if (!memberId.equals(clientId)) {
                    return memberId;
                }
            }
        }
        //no other member found
        return null;
    }
    //checks whether another member is available for private messaging, 
    //returns true if another member exists, false otherwise. 
    public boolean hasAnotherMemberAvailable() {
        return getFirstOtherMemberIdFromLastMemberList() != null;
    }
    //sends a private message to the first other member found
    //in the most recent member list. 
    public void sendPrivateMessageToFirstOtherMember(String text) throws Exception {
    	//find a target member.
        String targetId = getFirstOtherMemberIdFromLastMemberList();
        //in no target exists, do nothing. 
        if (targetId == null) {
            System.out.println("[CLIENT] No other member available for private message.");
            return;
        }

        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        Message message = new Message(
                MessageType.PRIVATE_MESSAGE,
                clientId,
                targetId + "|" + text
        );
        //send the private message request.
        out.writeObject(message);
        out.flush();

        System.out.println("[CLIENT] Sent PRIVATE_MESSAGE to " + targetId);

        Message response = (Message) in.readObject();
        System.out.println("[CLIENT] " + response.getContent());

        socket.close();
    }
    //send a broadcast message to all other members.
    public void sendBroadcastMessage(String text) throws Exception {

        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //create the broadcast message. 
        Message message = new Message(
                MessageType.BROADCAST_MESSAGE,
                clientId,
                text
        );
        //send it to the server. 
        out.writeObject(message);
        out.flush();

        System.out.println("[CLIENT] Sent BROADCAST_MESSAGE");
        //read server acknowledgement.
        Message response = (Message) in.readObject();
        System.out.println("[CLIENT] " + response.getContent());

        socket.close();
    }
    //request all pending inbox messages for this client. 
    public void requestInbox() throws Exception {

        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //build inbox request.
        Message request = new Message(
                MessageType.INBOX_REQUEST,
                clientId,
                "Requesting inbox"
        );
        //send request. 
        out.writeObject(request);
        out.flush();

        System.out.println("[CLIENT] Sent INBOX_REQUEST as " + clientId);
        //read response. 
        Message response = (Message) in.readObject();
        //if inbox response is received, print it. 
        if (response.getType() == MessageType.INBOX_RESPONSE) {
            System.out.println("[CLIENT] Inbox received:");
            System.out.println(response.getContent());
        }

        socket.close();
    }
    //sends a ping to the server and waits for a PONG.
    public void sendPing() throws Exception {

        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        //creates the PING message. 
        Message ping = new Message(
                MessageType.PING,
                clientId,
                "Ping from " + clientId
        );
        //sends PING.
        out.writeObject(ping);
        out.flush();
        //waits for PONG.
        Message response = (Message) in.readObject();

        if (response.getType() == MessageType.PONG) {
            // Keep ping output minimal for cleaner demo logs
            System.out.println("[CLIENT] PING/PONG OK for " + clientId);
        }

        socket.close();
    }
    //sends a LEAVE message to the server.
    public void leave() throws Exception {
        Socket socket = new Socket(serverHost, serverPort);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        Message leave = new Message(
                MessageType.LEAVE,
                clientId,
                "Leaving group"
        );
        //send it.
        out.writeObject(leave);
        out.flush();

        System.out.println("[CLIENT] Sent LEAVE as " + clientId);

        socket.close();
    }
}