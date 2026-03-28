package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import coordinator.CoordinatorService;
import model.MemberInfo;
import model.Message;
import model.MessageType;

public class ClientHandler implements Runnable {

    private final Socket socket;
    //shared service object containing group state and messaging logic.
    private final CoordinatorService coordinatorService;

    public ClientHandler(Socket socket, CoordinatorService coordinatorService) {
        this.socket = socket;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message message = (Message) in.readObject();

            if (message.getType() == MessageType.JOIN) {
                String ip = socket.getInetAddress().getHostAddress();
                int port = socket.getPort();

                //log the join request.
                System.out.println("[SERVER] JOIN received from " + message.getSenderId());

                //create a new member record for this client.
                MemberInfo member = coordinatorService.createMember(message.getSenderId(), ip, port);
                //add the member to the shared coordinator service.
                coordinatorService.handleJoin(member);
                //get the current coordinator after this join.
                MemberInfo coordinator = coordinatorService.getCoordinatorMember();
                //create the welcome message. 
                Message welcome = new Message(
                        MessageType.WELCOME,
                        "SERVER",
                        coordinator == null ? "none" : coordinator.toString()
                );
                //send the welcome message back to the client.
                out.writeObject(welcome);
                out.flush();
                //log that the welcome message was sent. 
                System.out.println("[SERVER] WELCOME sent to " + member);
            }

            if (message.getType() == MessageType.MEMBER_LIST_REQUEST) {
                System.out.println("[SERVER] MEMBER_LIST_REQUEST received from " + message.getSenderId());
                //ask the coordinator service for a formatted member list
                String memberList = coordinatorService.getMemberListAsString();
                //wrap the list inside a response message
                Message response = new Message(
                        MessageType.MEMBER_LIST_RESPONSE,
                        "SERVER",
                        memberList
                );
                //send the response back to the client.
                out.writeObject(response);
                out.flush();
                //log delivery.
                System.out.println("[SERVER] MEMBER_LIST_RESPONSE sent to " + message.getSenderId());
            }

            if (message.getType() == MessageType.PRIVATE_MESSAGE) {
                //log the receipt of private message request.
            	System.out.println("[SERVER] PRIVATE_MESSAGE received from " + message.getSenderId());
            	//content format is: targetId|actual message text.
                String content = message.getContent();
                int separatorIndex = content.indexOf('|');
                //extract client ID.
                String targetId = content.substring(0, separatorIndex);
                //extract actual private message text.
                String privateText = content.substring(separatorIndex + 1);
                //ask the coordinator service to deliver the private message.
                boolean success = coordinatorService.sendPrivateMessage(
                        message.getSenderId(),
                        targetId,
                        privateText
                );
                //create the response object to acknowledge success/failure.
                Message response;

                if (success) {
                    response = new Message(
                            MessageType.ACK,
                            "SERVER",
                            "Private message delivered."
                    );
                } else {
                    response = new Message(
                            MessageType.ERROR,
                            "SERVER",
                            "Private message failed. Target not found."
                    );
                }
                //send the response back to the sender
                out.writeObject(response);
                out.flush();
            }

            if (message.getType() == MessageType.BROADCAST_MESSAGE) {
                System.out.println("[SERVER] BROADCAST_MESSAGE received from " + message.getSenderId());

                int delivered = coordinatorService.sendBroadcastMessage(
                        message.getSenderId(),
                        message.getContent()
                );
                //create ACK response showing how many members received it.
                Message response = new Message(
                        MessageType.ACK,
                        "SERVER",
                        "Broadcast delivered to " + delivered + " member(s)."
                );
                //send response back to the sender
                out.writeObject(response);
                out.flush();
            }

            if (message.getType() == MessageType.INBOX_REQUEST) {
                System.out.println("[SERVER] INBOX_REQUEST received from " + message.getSenderId());

                String inbox = coordinatorService.getInboxAsString(message.getSenderId());
                //wrap text in a response message.
                Message response = new Message(
                        MessageType.INBOX_RESPONSE,
                        "SERVER",
                        inbox
                );
                //send the text back to the client.
                out.writeObject(response);
                out.flush();

                System.out.println("[SERVER] INBOX_RESPONSE sent to " + message.getSenderId());
            }

            if (message.getType() == MessageType.PING) {
                //update the last seen timestamp for this member.
                coordinatorService.recordHeartbeat(message.getSenderId());
                //log that a ping was received.
            	System.out.println("[SERVER] PING received from " + message.getSenderId());
            	//respond with a PONG message.
                Message response = new Message(
                        MessageType.PONG,
                        "SERVER",
                        "PONG for " + message.getSenderId()
                );
                //send PONG to the client.
                out.writeObject(response);
                out.flush();

                System.out.println("[SERVER] PONG sent to " + message.getSenderId());
            }

            if (message.getType() == MessageType.LEAVE) {
                //log leave request
            	System.out.println("[SERVER] LEAVE received from " + message.getSenderId());
                //remove the member and re-elect coordinator if needed. 
            	coordinatorService.handleLeave(message.getSenderId());
            }

            socket.close();

        } catch (Exception e) {
            //log any errors that happened during this handler (if any).
        	System.out.println("[HANDLER] Error: " + e.getMessage());
        }
    }
}