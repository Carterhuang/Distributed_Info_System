import java.util.List;

public interface View {
	//get ip
	public String getSelfIP();
	//get all server ips in this view
	public List<String> getViews();
	//get server ip in this view
	public String getView(int IpIndex);
	//add view
	public void addView(String IP);
	//add views in list
	public void addViews(List<String> views);
	//get current view size
	public int getViewSz();
	//remove an IP from this view
	public void remove(int IpIndex);
	//remove views
	public void clearView();
	//while v contains more than k entries, delete an entry chosen uniformly at random
	public void shrinkView(int k);
	//rm dup
	public void rmDup();
	//serialize view
	public String getSerialView();
	//get random view
	public String getRandomView();
}