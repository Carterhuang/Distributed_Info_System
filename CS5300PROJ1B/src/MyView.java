
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MyView implements View{
	private String IP; // 
	List<String> views = new ArrayList<String>(); //list of views
	Random rand;
	//Constructor
	public MyView(String IP){
		this.IP = IP;
		this.rand = new Random();
	}
	
	public MyView(){
		this.IP = "FAKE";
		this.rand = new Random();
	}
	
	public String getRandomView() {
		if (this.getViewSz() == 0) { return "0.0.0.0"; }
		int index = rand.nextInt(this.getViewSz());
		return views.get(index);
	}
	
	public String getRandomView(String pr) {
		if (this.getViewSz() == 1 && pr.equals(this.getRandomView()))  
			return "0.0.0.0"; 
		String bk = "";
		do {
			bk = this.getRandomView() ;
		} while (pr.equals(bk));
		return bk;
	}
	

	@Override
	public String getSelfIP() {
		return IP;
	}

	@Override
	public List<String> getViews() {
		return views;
	}
	
	@Override
	public String getView(int IpIndex) {
		return views.get(IpIndex);
	}

	@Override
	public void addView(String ip) {
		if (!ip.equals(this.IP))
			views.add(ip);
		rmDup();
	}
	
	@Override
	public void addViews(List<String> v) {
		views.addAll(v);
		rmDup();
	}

	@Override
	public int getViewSz() {
		return views.size();
	}

	@Override
	public void remove(int IpIndex) {
		views.remove(IpIndex);
	}

	@Override
	public void clearView() {
		views.clear();
	}

	@Override
	public void shrinkView(int k) {
		while (this.getViewSz() > k){
			int index =  rand.nextInt(this.getViewSz()); 
			this.remove(index);
		}
		
	}

	@Override
	public void rmDup() {
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(views);
		views.clear();
		views.addAll(set);
	}
	
	public String getSerialView() {
    	String serial = "";
    	if (views.size() == 0) return serial;
    	for( String ip : views )
    		serial += (ip + "#");
    	return serial.substring(0, serial.length()-1);
	}

	
}