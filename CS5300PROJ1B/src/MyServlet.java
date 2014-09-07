import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Servlet implementation class MyServlet
 */
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String defaultMsg = "DEFAULT MESSAGE, PLEASE REPLACE";
    public static ConcurrentHashMap<String, MySession> mytable = null;
    public static String localIP = null;
    public static RPCServer rpcServer;
    public static RPCClient rpcClient;
    /**
     * Default constructor. 
     */
	
    public MyServlet() {
        // TODO Auto-generated constructor stub
    }
    
    public void init(ServletConfig config) throws ServletException {	
    	super.init(config);
    	//localIP = "127.0.0.1"; // java code to retrieve ip address
    	localIP = (String) config.getServletContext().getAttribute("localip");	
    	rpcClient = new RPCClient( localIP, 5200 );
    }
    
    
    // OK
    public void cleanTable(ConcurrentHashMap<String, MySession> table) {
    	for (String key : table.keySet()) {
    		MySession tmp = table.get(key);
    		if (tmp.getExpireTime() < System.currentTimeMillis()) {
    			System.out.println("remove key: " + key);
    			System.out.println("remove session " + table.get(key).toString() );
    			table.remove(key);
    		}
    	}
    }
  
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("doGet called");
		@SuppressWarnings("unchecked")
		ConcurrentHashMap<String, MySession> mytable = (ConcurrentHashMap<String, MySession>) 
				getServletContext().getAttribute("sessiontable");		
		
		cleanTable(mytable);

		
		// This is the default output message
		String refreshMsg = defaultMsg;
		MyView localView = (MyView) getServletContext().getAttribute("myview");
		MySession currSession = null;
		String sessID = null;
		int version = 0;
		String serverPR = "0.0.0.0";
		String serverBK = "0.0.0.0";
		
		// Find Cookie
		Cookie[] resCookies = request.getCookies();
		Cookie myCookie = null;
		if (resCookies != null) {
			for (Cookie c: resCookies) {
				if ("CS5300Project1".equals(c.getName()))
					myCookie = c;
			}
		}
		
		
		// When user click on "Refresh" ...
		if (request.getParameter("action") != null && 
					request.getParameter("action").equals("refresh")) {
			
			if (myCookie != null) {
				
				System.out.println("For refresh, cookie exists");
				
				String strArr[] = myCookie.getValue().split("_");
				sessID = strArr[0];
				version = Integer.parseInt(strArr[1]);
				serverPR = strArr[2];
				serverBK = strArr[3];
				currSession = mytable.get(sessID);
				
				
				System.out.println("doGet Cookie: " +  sessID);
				
				if (currSession != null) { // with cookie and has session in local table
					// update refresh message
					refreshMsg = currSession.getValue();
					// update local session
					currSession.incrementVersion();
					currSession.updateExpireTime(); 
					// update remote session
					mytable.put(sessID, currSession);
					String destIP = (localIP.equals(serverPR)) ? serverBK : serverPR;
					rpcClient.sessionWrite(destIP, currSession); // update  BACKUP
					myCookie.setMaxAge((int)MySession.expire);
				}
				else { // refresh and cookie exists, but no session in the table
					String sessMsg = rpcClient.sessionRead(sessID, version, serverPR, serverBK); // read
					if (sessMsg != null)
						refreshMsg = sessMsg.split("#")[2];
				}
			}
			
			else {
				refreshMsg = defaultMsg;
			}	
			
			request.setAttribute("expire", MySession.expire);
			request.setAttribute("discard", System.currentTimeMillis() + MySession.expire * 1000L);
		}
		
		// When user click on "Logout"
		else if (request.getParameter("action") != null &&
				request.getParameter("action").equals("logout")) {
			// Logout functionality
			// Delete session corresponds to that cookie
			System.out.println("logging out ...");
			
			// if cookie exists
			if (myCookie != null) {
				String strArr[] = myCookie.getValue().split("_");
				sessID = strArr[0];  // ip$num
				version = Integer.parseInt(strArr[1]);
				serverPR = strArr[2];
				serverBK = strArr[3];
				myCookie.setMaxAge(0);
				currSession = mytable.get(sessID);
				

				// currSession in local table
				if (currSession != null) {
					currSession.makeExpire();
					mytable.put(sessID, currSession);
					String destIP = (localIP.equals(serverPR)) ? serverBK : serverPR;
					rpcClient.sessionWrite( destIP, currSession);
				}
				
				else {
					// cloning session and make it expire
					currSession =  new MySession(sessID , version, refreshMsg, System.currentTimeMillis());
					rpcClient.sessionWrite( serverPR, currSession );
					rpcClient.sessionWrite( serverBK, currSession );
				}
			}
			request.setAttribute("expire", MySession.expire);
			request.setAttribute("discard", System.currentTimeMillis());
		}	
		
		// Finally add Cookie
		if (myCookie != null)
			response.addCookie(myCookie);
		localView = (MyView) getServletContext().getAttribute("myview");

		// update web-page
		getServletContext().setAttribute("sessiontable", mytable);
		request.setAttribute("serverID", localIP);
		request.setAttribute("serverPR", serverPR);
		request.setAttribute("serverBK", serverBK);
		request.setAttribute("view", localView.getSerialView());
		request.setAttribute("replace", refreshMsg);
		RequestDispatcher rd = request.getRequestDispatcher("testServletPage.jsp");
		rd.forward(request, response);
	}
	

	
	
	
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("doPost called");
		@SuppressWarnings("unchecked")
		ConcurrentHashMap<String, MySession> mytable = (ConcurrentHashMap<String, MySession>) 
				getServletContext().getAttribute("sessiontable");
		
		cleanTable(mytable);
		
		MyView localView = (MyView) getServletContext().getAttribute("myview");
		MySession currSession = null;
		String serverPR = "0.0.0.0";
		String serverBK = "0.0.0.0";
		
		
		// Determine output message
		String returnMsg = request.getParameter("newMessage");
		if (returnMsg == null || returnMsg.equals(""))
			returnMsg = defaultMsg;

		
		// Cookie finding
		Cookie[] resCookies = request.getCookies();
		Cookie myCookie = null;
		for (Cookie c : resCookies) {
			if ("CS5300Project1".equals(c.getName())) 
				myCookie = c;
		}
		
		// if no 5300 cookie
		if ( myCookie == null ) {
			
			System.out.println("New Session created");
			
			serverPR = localIP;
			serverBK = localView.getRandomView(serverPR);
			// create primary
			currSession = new MySession(serverPR, returnMsg);
//			System.out.println("sessID after replace: " + currSession.getID());
			System.out.println("doPost: " + currSession.getID());
			
			mytable.put(currSession.getID(), currSession);
			String cookieVal = currSession.getID() + "_" + 0 + "_" +
					   serverPR + "_" + serverBK;
			myCookie = new Cookie("CS5300Project1", cookieVal);
			myCookie.setMaxAge((int)MySession.expire);
			// write to backup
			rpcClient.sessionWrite(serverBK, currSession);
			mytable.get("replace msg: " + currSession.getID());
		}
		
		
		else {
			myCookie.setMaxAge((int)MySession.expire);
			String[] strArr = myCookie.getValue().split("_");
			String sessId = strArr[0];
//			System.out.println("sessID after replace: " + sessId);

			int version = Integer.parseInt(strArr[1]);
			serverPR = strArr[2];
			serverBK = strArr[3];
			MyView myview = null;
			String sessID = myCookie.getValue().split("_")[0];
			currSession = mytable.get(sessID);
			
			// currSession is in the table
			if (currSession != null) {
				// update local session
				currSession.incrementVersion();
				currSession.updateExpireTime();
				currSession.setOutputMsg(returnMsg);
				mytable.put(sessID, currSession);
				String destIP = (localIP.equals(serverPR)) ? serverBK : serverPR;
				rpcClient.sessionWrite(destIP, currSession);
			}
			// currSession not in the local table
			else {
				currSession = new MySession(sessID, 
											version + 1,
											returnMsg,
											System.currentTimeMillis() + MySession.expire * 1000L);
				rpcClient.sessionWrite(serverPR, currSession);
				rpcClient.sessionWrite(serverBK, currSession);
			}
		}
	
		// Finally add Cookie
		if (myCookie != null)
			response.addCookie(myCookie);
		request.setAttribute("expire", MySession.expire);
		request.setAttribute("discard", System.currentTimeMillis() + MySession.expire * 1000L);
		localView = (MyView) getServletContext().getAttribute("myview");
		getServletContext().setAttribute("sessiontable", mytable);
		request.setAttribute("serverID", localIP);
		request.setAttribute("serverPR", serverPR);
		request.setAttribute("serverBK", serverBK);
		request.setAttribute("view", localView.getSerialView());
		request.setAttribute("replace", returnMsg);
		RequestDispatcher rd = request.getRequestDispatcher("testServletPage.jsp");
		rd.forward(request, response);	    
	}
}