import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

public class GarbageCollect extends Thread{
	private long freq;
	private volatile boolean isRunning;
	private ServletContext context;

	//constructor
	public GarbageCollect(long freq, ServletContext context){
		this.freq = freq;
		this.context = context;
	}

	//run GarbageCollect thread
	@SuppressWarnings("unchecked")
	public void run() {
		while (isRunning) {
			try {
				ConcurrentHashMap<String, MySession> ST = new ConcurrentHashMap<String, MySession>();
				ST = (ConcurrentHashMap<String, MySession>) context.getAttribute("SessionTable");
				//check expire time and clean table
				for (String key:ST.keySet()){
					if (ST.get(key).getExpireTime() < System.currentTimeMillis()) {
						ST.remove(key);
					}
				}
				context.setAttribute("SessionTable", ST);
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	//terminate GarbageCollect thread
	public void terminate() {
		isRunning = false;
	}


}