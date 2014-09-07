import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;


public class RPCServer extends Thread {

	public static volatile boolean running = true;
	public static ConcurrentHashMap<String, MySession> mytable = null;
	
	public static DatagramSocket rpcSocket;
	public static DatagramPacket recvPkt;
	public static int serverPort;
	public static int returnPort;
	public static byte[] inBuf = null;
	public static byte[] outBuf = new byte[512];
	public static InetAddress returnAddr = null;
	public static String callID = null;
	public static String sessID = null;
	public static String sessVal = null;
	public static int operationCode = 0;
	public static String recvMsg = null;
	public static String sendMsg = null;
	public static ServletContext context = null;
	
	public RPCServer( ServletContext sc ) {
		context = sc;
	}
	
	public void killThread() {
		running = false;
		rpcSocket.close();
	}
	
	
	@Override
	public void run() {
		
		System.out.println("Server initiating ...");
		
		try {
			rpcSocket = new DatagramSocket(5300);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		while(running) {
			listen();
		}
	}
	
	public void listen() {
		
		System.out.println("listening");	
		ConcurrentHashMap<String, MySession> mytable = 
						(ConcurrentHashMap<String, MySession>) context.getAttribute("sessiontable");
		MyView myView = (MyView) context.getAttribute("myview");
		
		inBuf = new byte[512];
		recvPkt = new DatagramPacket(inBuf, inBuf.length);
		
		try { // receiving packet
			rpcSocket.receive(recvPkt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (recvPkt == null) { 
			System.out.println("packet receiving failed");
			return; 
		}
	
		returnAddr = recvPkt.getAddress();
		returnPort = recvPkt.getPort();
		
		try { // from bytes to string
			recvMsg = new String(inBuf, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		System.out.println("Server: recvMsg is: " + recvMsg);
		recvMsg = recvMsg.trim();
		String[] msgArr = recvMsg.split("@");
		callID = msgArr[0];
		operationCode = Integer.parseInt(msgArr[1]);
		String[] argArr = msgArr[2].split("#");
		
		MySession newSession = null;
		
		switch ( operationCode ) {		
		case 1: // update & store
			newSession = new MySession(argArr[0], 
									Integer.parseInt(argArr[1]),
									argArr[2],
									Long.parseLong( argArr[3].replaceAll("[^0-9]", "")));
			mytable.put(newSession.getID(), newSession);
			sendMsg = callID + "@" + operationCode + "@" + "Write complete";
			break;
		case 2: // read
			String sessID = argArr[0];
			newSession = mytable.get(sessID);
			if (newSession == null) {
				newSession = new MySession("0.0.0.0", 0, null, 0);
			}
			newSession.incrementVersion();
			newSession.updateExpireTime();
			mytable.put(sessID, newSession);
			sendMsg = callID + "@" + operationCode + "@" + newSession.toString();
			break;
		case 3: // for gossip protocol, receive ip and save it in the view
			if (!myView.getSelfIP().equals(msgArr[2]))
			myView.addView(msgArr[2]);
			sendMsg = callID + "@" + operationCode + "@" + myView.getSerialView();
			break;
		}
		context.setAttribute("sessiontable", mytable);
		context.setAttribute("myview", myView);
		
		outBuf = sendMsg.getBytes();
    	DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
    	try {
			rpcSocket.send(sendPkt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void terminate() {
		running = false;
		rpcSocket.close();
	}
	

}
