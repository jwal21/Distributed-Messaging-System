package model;
//MessageType defines all requests and response kinds used in the 
//client-server protocol.
public enum MessageType {
    JOIN,					//client joins the group
    LEAVE,					//client leaves the group
    WELCOME,				//server tells client who the coordinator is
    MEMBER_LIST_REQUEST,	//client asks for all current members
    MEMBER_LIST_RESPONSE,	//server replies with member list
    PRIVATE_MESSAGE,		//client sends a private message
    BROADCAST_MESSAGE,		//client sends a broadcast message
    INBOX_REQUEST,			//client asks for received messages
    INBOX_RESPONSE,			//server replies with inbox contents
    ACK,					//generic positive acknowledgement
    ERROR,					//generic error response
    PING,					//liveness check request
    PONG					//liveness check reply
}