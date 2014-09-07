import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * 
 * @author xiaoanyan
 * Implementing gossip protocol 
 * Initialize and maintain each server's view
 *
 */

public class MyGossip extends Thread{
	private static int freq = 100; //view updating frequency
	private static volatile boolean run_state = true; //terminate flag
	private MySimpleDB client; 
	private static ServletContext context;
	//private static Fake_ServletContext context;
	Random rand;
	private static String domainName = "bootStrap";
	private static String itemName = "basicView";
	private static int viewSize = 5;
	private static String colName = "Col";
	
	//Constructor
	public MyGossip(ServletContext ct){
		context = ct;
		// secret key and token specified inside this DB
		this.client = new MySimpleDB(); 
		this.rand = new Random();
	}

	public void run() {
		while(run_state) {
			gossipDB();  		 // to be implemented
			gossipOther();       // to be implemented
			try {
				Thread.sleep(freq + rand.nextInt(freq));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	// update my View
	public void gossipDB() {
		
		//System.out.println("gossipDB");
		
		MyView localView = (MyView) context.getAttribute("myview");		
		String dbViews = client.getValue(domainName, itemName, colName);
		String[] strArr = dbViews.split("_");

		// update self
		// remove dup
		MyView dbView = new MyView();
		dbView.addViews(Arrays.asList(strArr));
		union(localView, dbView);
		
		//update DB 
		dbView.addView(localView.getSelfIP());
		dbView.shrinkView(viewSize);
		String dbValue = "";
		for (String str1 : dbView.getViews()) {
			dbValue += str1 + "_";
		}
		dbValue = dbValue.substring(0, dbValue.length() - 1);
		//put back to bootStrap
		client.putData(domainName, colName, itemName, dbValue);
		//System.out.println("DB after updating: " + client.getValues(domainName, itemName).toString());
	}
	
	

	// gossip with another random server
	public void gossipOther() {
		
		//System.out.println("gossipOther");
		//get my view
		MyView localView = (MyView) context.getAttribute("myview");
		
		// get other's view
		String targetIP = null;
		if (localView.getViewSz() == 0) {
			return;
		}
		targetIP = getIP(localView);
		
		String localIP = (String) context.getAttribute("localip");
		RPCClient myClient = new RPCClient( localIP, 8888 );
		MyView targetView = myClient.getView(targetIP);
		union(localView, targetView);

		//System.out.println("local ip: " + localView.getSelfIP());
		//System.out.println("Gossip view: " + localView.getSerialView());
		
		
		context.setAttribute("myview", localView);
	}
	// update self views
	public void updateSelf() {
		
	}

	public void terminate(){
		run_state = false;
	}

	//insert s into v if not already present
	public void insert(MyView v, String IP){
		if (!v.getViews().contains(IP)){
			v.addView(IP);
		}
	}

	//remove s from v if it is present
	public void remove(MyView v, String IP){
		v.remove(v.getViews().indexOf(IP));
	}


	//return s chosen uniformly at random from v
	public String getIP(MyView v){
		if (v.getViewSz() == 0) { return "0.0.0.0"; }
		int index = rand.nextInt(v.getViewSz());
		return v.getView(index);
	}

	//set v to the union of v and w (eliminating duplicates)
	public void union(MyView v, MyView w){
		Set<String> hs = new LinkedHashSet<String>();
		List<String> l = new LinkedList<String>();
		hs.addAll(v.getViews());
		hs.addAll(w.getViews());
		if (hs.contains(v.getSelfIP())) hs.remove(v.getSelfIP());
		l.addAll(hs);
		v.clearView();
		v.addViews(l);
		String selfIP = v.getSelfIP();
		if (v.getViews().contains(selfIP)){
			remove(v, selfIP); //caution
		}
		v.shrinkView(viewSize);
	}
}


