package coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.MemberInfo;
import util.TimeUtil;

public class CoordinatorService {
	//LinkedHashMap preserves insertion order. this is useful because when
	//the coordinator leaves, the next inserted member can become the new coordinator. 
    private final Map<String, MemberInfo> members = new LinkedHashMap<>();
    private final Map<String, List<String>> inboxes = new HashMap<>();
    private final Map<String, Long> lastSeen = new HashMap<>();


    //stores the current coordinator's client ID.
    private String coordinatorId = null;

    //pool of readable names to be randomly assigned to the members.
    private static final String[] NAMES = {"Juan", "Jude", "Leo", "Yusuf"};
    //random object used to assign a name.
    private final Random random = new Random();
    //picks a random name from the name pull and returns one at random.
    private String getRandomName() {
        return NAMES[random.nextInt(NAMES.length)];
    }

    public synchronized MemberInfo createMember(String clientId, String ip, int port) {
        //assign a readable name.
    	String name = getRandomName();
    	//return the populate member object.
        return new MemberInfo(clientId, name, ip, port);
    }

    public synchronized void handleJoin(MemberInfo member) {
    	//add member to the active-members map.
        members.put(member.getId(), member);
        //update last seen timestamp for this member.
        lastSeen.put(member.getId(), System.currentTimeMillis());
        //ensure every member has an inbox ready.
        inboxes.putIfAbsent(member.getId(), new ArrayList<>());

        //if no coordinator exists yet, the first member becomes coordinator.
        if (coordinatorId == null) {
            coordinatorId = member.getId();
            System.out.println("[COORD] " + member + " is now the coordinator (first member).");
        } else {
            //otherwise just log the join while keeping the old coordinator.
        	System.out.println("[COORD] " + member + " joined. Current coordinator: " + members.get(coordinatorId));
        }
        //print current active members.
        System.out.println("[COORD] Members now: " + members.values());
    }
    
    
    //remove a member from the group, if the member was the coordinator,
    //elect a new one automatically. 
    public synchronized void handleLeave(String memberId) {
    	//if member does not exist, do nothing.
        if (!members.containsKey(memberId)) {
            return;
        }
        //remove member from active map and keep a reference for logging.
        MemberInfo leaving = members.remove(memberId);
        //remove member's last seen timestamp.
        lastSeen.remove(memberId);
        //remove the member's inbox too.
        inboxes.remove(memberId);

        System.out.println("[COORD] " + leaving + " left the group.");
        //if the leaving member was the coordinator, elect the next available
        //member as the new coordinator. 
        if (memberId.equals(coordinatorId)) {
            coordinatorId = members.keySet().stream().findFirst().orElse(null);

            if (coordinatorId != null) {
                System.out.println("[COORD] New coordinator elected: " + members.get(coordinatorId));
            } else {
                System.out.println("[COORD] No members left.");
            }
        }
        //print current active members after the removal.
        System.out.println("[COORD] Members now: " + members.values());
    }
    
    //updates the last seen timestamp for a member- to track active members.
    public synchronized void recordHeartbeat(String memberId) {
        if (members.containsKey(memberId)) {
            lastSeen.put(memberId, System.currentTimeMillis());
        }
    }

    //removes members that have not sent a heartbeat within the specified timeout.
    public synchronized void removeTimedOutMembers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        List<String> timedOut = new ArrayList<>();

        for (String memberId : members.keySet()) {
            Long seen = lastSeen.get(memberId);
            if (seen != null && (now - seen) > timeoutMillis) {
                timedOut.add(memberId);
            }
        }

        for (String memberId : timedOut) {
            System.out.println("[FAULT] Member timed out: " + memberId);
            handleLeave(memberId);
        }
    }


    //returns the current coordinator's ID, or null if no coordinator exists.
    public synchronized String getCoordinatorId() {
        return coordinatorId;
    }

    public synchronized MemberInfo getCoordinatorMember() {
        if (coordinatorId == null) {
            return null;
        }
        return members.get(coordinatorId);
    }

    public synchronized String getMemberListAsString() {

    	//build response using StringBuilder for efficiency.
        StringBuilder builder = new StringBuilder();
        //add coordinator information.
        MemberInfo coordinator = getCoordinatorMember();
        builder.append("Coordinator: ")
               .append(coordinator == null ? "none" : coordinator.toString())
               .append("\n");
        //add members heading.
        builder.append("Members:\n");
        //append each active member.
        for (MemberInfo member : members.values()) {
            builder.append("- ID: ").append(member.getId())
                   .append(", Name: ").append(member.getName())
                   .append(", IP: ").append(member.getIp())
                   .append(", Port: ").append(member.getPort())
                   .append("\n");
        }

        return builder.toString();
    }
    //sends a private message from one member to another, returns true
    //if delivery was successful, false otherwise. 
    public synchronized boolean sendPrivateMessage(String senderId, String targetId, String text) {
    	//if either sender or receiver is missing, delivery fails.
        if (!members.containsKey(senderId) || !members.containsKey(targetId)) {
            return false;
        }
        //get sender and target details.
        MemberInfo sender = members.get(senderId);
        MemberInfo target = members.get(targetId);
        //build a timestamped log line
        String logLine = "[" + TimeUtil.now() + "] PRIVATE from "
                + sender + " to " + target + ": " + text;
        //add the messages to the target's inbox
        inboxes.get(targetId).add(logLine);
        //log on the server console too.
        System.out.println("[SERVER] " + logLine);
        return true;
    }
    //sends a broadcast message from one member to all other members,
    //returns number of recipients the message was delivered to. 
    public synchronized int sendBroadcastMessage(String senderId, String text) {

    	//if sender is not active, no broadcast is possible. 
        if (!members.containsKey(senderId)) {
            return 0;
        }
        //get sender details.
        MemberInfo sender = members.get(senderId);
        //count how many members receive the broadcast.
        int delivered = 0;
        //send to every active member except the sender.
        for (MemberInfo member : members.values()) {
            if (!member.getId().equals(senderId)) {
                String logLine = "[" + TimeUtil.now() + "] BROADCAST from "
                        + sender + " to " + member + ": " + text;
                //store broadcast in the recipient inbox
                inboxes.get(member.getId()).add(logLine);
                //increment delivery count
                delivered++;
            }
        }
        //log summary on the server
        System.out.println("[SERVER] Broadcast from " + sender + " delivered to " + delivered + " member(s).");
        return delivered;
    }
    //returns and clears the inbox for a given client.
    public synchronized String getInboxAsString(String clientId) {
    	//if no inbox exists for this client, return a fallback message. 
        if (!inboxes.containsKey(clientId)) {
            return "No inbox available.";
        }
        //retrieve the inbox list.
        List<String> inbox = inboxes.get(clientId);
        //in no messages exist, return a clear message.
        if (inbox.isEmpty()) {
            return "No messages.";
        }
        //build a full inbox string.
        StringBuilder builder = new StringBuilder();
        for (String msg : inbox) {
            builder.append(msg).append("\n");
        }
        //clear inbox after it has been read. 
        inbox.clear();
        return builder.toString();
    }

    // -------------------------
    // Helper methods for testing :D
    // -------------------------

    public synchronized int getMemberCount() {
        return members.size();
    }

    public synchronized boolean hasMember(String memberId) {
        return members.containsKey(memberId);
    }
}