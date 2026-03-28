//import JUnit assertion methods. 
import static org.junit.Assert.*;
//import setup and test annotations. 
import org.junit.Before;
import org.junit.Test;
import coordinator.CoordinatorService;
//import MemberInfo to create test members.
import model.MemberInfo;

public class CoordinatorServiceTest {
	//service under test.
    private CoordinatorService service;
    //reusable test members.
    private MemberInfo memberA;
    private MemberInfo memberB;

    //runs before each test to create a fresh CoordinatorService
    //and runs fresh test members.
    @Before
    public void setUp() {
        service = new CoordinatorService();

        memberA = new MemberInfo("client-A", "Juan", "127.0.0.1", 5001);
        memberB = new MemberInfo("client-B", "Jude", "127.0.0.1", 5002);
    }
    //test 1 verifies that the first member to join becomes the coordinator.
    @Test
    public void firstMemberBecomesCoordinator() {
        service.handleJoin(memberA);
        //coordinator should now be memberA.
        assertEquals("client-A", service.getCoordinatorId());
        //group should contain exactly one member.
        assertEquals(1, service.getMemberCount());
        //the member should be present.
        assertTrue(service.hasMember("client-A"));
    }
    //test 2 verifies that when a second member joins,
    //the original first member remains coordinator. 
    @Test
    public void secondMemberJoinsAndFirstRemainsCoordinator() {
    	//add first and second members. 
        service.handleJoin(memberA);
        service.handleJoin(memberB);
        //coordinator should still be the first member.
        assertEquals("client-A", service.getCoordinatorId());
        //there should now be 2 members. 
        assertEquals(2, service.getMemberCount());
        //both members should exist in the group.
        assertTrue(service.hasMember("client-A"));
        assertTrue(service.hasMember("client-B"));
    }
    //test 3 verifies that when the coordinator leaves, the next
    //member becomes the new coordinator.
    @Test
    public void coordinatorLeavesAndNextMemberBecomesCoordinator() {
    	//add two members.
        service.handleJoin(memberA);
        service.handleJoin(memberB);
        //remove the first member (current coordinator).
        service.handleLeave("client-A");
        //coordinator should switch to the second member.
        assertEquals("client-B", service.getCoordinatorId());
        //only one member should remain
        assertEquals(1, service.getMemberCount());
        //old member should be gone; new one should still exist.
        assertFalse(service.hasMember("client-A"));
        assertTrue(service.hasMember("client-B"));
    }
    //test 4 verifies that private and broadcast messages are delivered
    //and appear in the receiver's inbox. 
    @Test
    public void privateAndBroadcastMessagesAppearInInbox() {
    	//add two members
        service.handleJoin(memberA);
        service.handleJoin(memberB);
        //send a private message from A to B.
        boolean privateDelivered = service.sendPrivateMessage("client-A", "client-B", "Hello Jude");
        //send a broadcast message from A (which should reach B)
        int broadcastDelivered = service.sendBroadcastMessage("client-A", "Hello everyone");
        //read B's inbox.
        String inboxB = service.getInboxAsString("client-B");
        //private message should succeed. 
        assertTrue(privateDelivered);
        //broadcast should be delivered to exactly one other member.
        assertEquals(1, broadcastDelivered);
        //inbox should contain not message types and their contents.
        assertTrue(inboxB.contains("PRIVATE"));
        assertTrue(inboxB.contains("Hello Jude"));
        assertTrue(inboxB.contains("BROADCAST"));
        assertTrue(inboxB.contains("Hello everyone"));
    }
}
