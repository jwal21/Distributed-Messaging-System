package model;
//Serializable allows MemberInfo objects to be sagely transferred if needed.
import java.io.Serializable;
//MemberInfo stores identifying information about one group member.
public class MemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    //unique client ID.
    private final String id;
    //readable display name.
    private final String name;
    //client IP address.
    private final String ip;
    //client port.
    private final int port;

    //constructs a new MemberInfo port.
    public MemberInfo(String id, String name, String ip, int port) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
    //returns the member ID
    public String getId() {
        return id;
    }
    //returns the name
    public String getName() {
        return name;
    }
    //returns the IP address
    public String getIp() {
        return ip;
    }
    //returns the port
    public int getPort() {
        return port;
    }

    //returns a readable string representation of the member.
    @Override
    public String toString() {
        return id + " (" + name + ")";
    }
}