import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/*
 *  RPCServerThread class
 */

public class MyServletContextListener implements ServletContextListener{

	public static ServletContext context;
	public RPCServer rpcServer;
	public MyGossip gossipThread; 
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent){
		// TODO Auto-generated method stub
		// initialize context used for all threads
		context = contextEvent.getServletContext();
		
		String localIP = "";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec( "/opt/aws/bin/ec2-metadata --public-ipv4" );
			BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()) );
			localIP = in.readLine();
			in.close();
		} catch (IOException e) {
				
		}
		localIP = localIP.split(":")[1].trim();
		
		//String localIP = "128.84.216.215"; // FAKE, TO BE REPLACED
		
		context.setAttribute("localip", localIP);
    	context.setAttribute("sessiontable", new ConcurrentHashMap<String, MySession>());
    	context.setAttribute("myview", new MyView(localIP));
		System.out.println("Listener Initialized ...");
		
		rpcServer = new RPCServer( context );
		rpcServer.start();
		
		gossipThread = new MyGossip(context);
		gossipThread.start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		// TODO Auto-generated method stub
		// clear dummy IP in BootStrap
		rpcServer.terminate();
		gossipThread.terminate();
		
	}
	
	/*
	 *  The listen method basically just receives packet 
	 *  and pass information
	 */
}
