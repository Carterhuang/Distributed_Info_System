import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;


public class RPCClient {
	
	private String localIP;
	private int localPort;
	private static int counter;
	
	
	public RPCClient( String ip, int port ) {
		localIP = ip;
		localPort = port;
		counter = 0;
	}
	
	 
    /*
     * When this method is called, the cookie value is
     * session_version_serverPrimary_serverBackup
     */
    public String sessionRead( String sessID, int ver, String serverPR, String serverBK ) throws IOException {
    	
    	System.out.println("sesseionRead called");
    	String sendData = sessID + "#" + ver;   	
    	String reply = null;
    	reply = rpcClientSend( serverPR, 2, sendData );
    	if (reply == null) {
    		reply = rpcClientSend( serverBK, 2, sendData );
    	}
    	return reply;
    }
    
    
    
    
    
    public void sessionWrite( String destIP, MySession session) {
    	
    	System.out.println("sesseionWrite called");    	
    	String reply =  null;
    	try {
    		System.out.println("sessionWrite data:" + session.toString());
			reply = rpcClientSend(destIP, 1 , session.toString() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("In sessionWrite reply is: ");
    	System.out.println(reply);
    	
    	if (reply == null)
    		System.out.println("session write failed");
    	else 
    		System.out.println("Write complete");
    }
    
    
	
    // data includes at least sessionID and version number
    // return received messages with callID trimmed
    // return message only includes "#" as delimiter
    public String rpcClientSend( String destIP, int opCode, String data ) throws IOException {
    	//System.out.println("rpcClientSend called");
    	//System.out.println("client sendto: " + destIP);
    	
    	String callID;    	
    	String localCallID = localIP + "$" + counter;
    	
    	DatagramSocket rpcSocket = new DatagramSocket();
    	
    	InetAddress destAddr = InetAddress.getByName(destIP);
    	String recvStr = null;
    	int recvOpCode; // the code to be determined in the loop
    	
    	String sendStr = localCallID + "@" + opCode  + "@" + data;
    	byte[] outBuf = sendStr.getBytes();
    	byte[] inBuf = new byte[512];
    	// 5300 is always the server port
    	DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, destAddr, 5300);
    	DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

    	try{
    		rpcSocket.send(sendPkt);
    	} catch (BindException be) {
    		rpcSocket.close();
    		return null;
    	}
    	try {
    		do {
    			recvPkt.setLength(inBuf.length);
    			rpcSocket.setSoTimeout(100); // timeout 2 s
    			
    			inBuf = new byte[512];
    			recvPkt = new DatagramPacket(inBuf, inBuf.length);
    			
    			rpcSocket.receive(recvPkt); // blocking
    			recvStr = new String(inBuf, "UTF-8");
    			callID = recvStr.split("@")[0]; // callID is strArr[0]
    			recvOpCode = Integer.parseInt(recvStr.split("@")[1]);
    		} while (!localCallID.equals(callID) || opCode != recvOpCode); // SKIP PACKETS WITH WRONG CALLID
    		
    	} catch (InterruptedIOException iioe) {
    		recvPkt = null;
    	} catch (IOException ioe) {
    		recvPkt = null;
    	} 
    	rpcSocket.close();
    	counter ++;
    	
    	if (recvPkt == null || recvStr == null) { return null; }
    	return recvStr.split("@")[2].trim();
    }
    
 
    public MyView getView(String targetIP) {
    	
    	MyView view = new MyView(targetIP);
		String responseMsg = null;
		try {
			responseMsg = rpcClientSend(targetIP, 3, localIP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			responseMsg = null;
		} 
		if (responseMsg == null) {
			//ystem.out.println("gossip failed, ip: " + localIP + " targetIP: " + targetIP);
		}
		//System.out.println("getView recvMsg: " + responseMsg);		
		if (responseMsg == null) 
			return view;
		String[] iplist = responseMsg.split("#");
		view.addViews(Arrays.asList(iplist));
    	return view;
    }
    
    
}
