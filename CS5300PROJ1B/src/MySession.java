
public class MySession {
	
	public static int autoIndex = 0;
	public static long expire = 10L;
	private int version;
	private static String ip = null;
	private long expireTime;
	private String sessionID;
	private int myVersion;
	private String sessionVal;
	
	// constructor for creating a new instance
	public MySession(String serverIP, String returnMsg) {
		// TODO Auto-generated constructor stub
		sessionID = serverIP + "$" + autoIndex;
		autoIndex++;
//		System.out.println("sessionID: " + sessionID);
		version = 0;
		sessionVal = returnMsg;
		expireTime = System.currentTimeMillis() + expire * 1000L;
	}
	
	// making a session to overwrite other session
	public MySession(String sessID, int ver, String msg, long expireT) {
		sessionID = sessID;
//		System.out.println("sessionID: " + sessionID);
		myVersion = ver;
		sessionVal = msg;
		expireTime = expireT;
	}
	
	public String getID() {
		return sessionID;
	}

	public String getValue() {
		return sessionVal;
	}
	
	public void updateExpireTime() {
		expireTime = System.currentTimeMillis() + expire * 1000L;
	}
	
	public void makeExpire() {
		expireTime = System.currentTimeMillis();
	}
	
	public long getExpireTime() {
		return expireTime;
	}	
		
	public static String getIndex() {
		return Integer.toString(autoIndex);
	}
	
	public void setOutputMsg(String msg) {
		sessionVal = msg;
	}

	public void incrementVersion() {
		myVersion ++;
	}
	
	public String toString () {
		String returnStr = "";
		returnStr = sessionID + "#" +
					myVersion + "#" +
					sessionVal+ "#" +
					expireTime+ "#";
		return returnStr;
	}


}
