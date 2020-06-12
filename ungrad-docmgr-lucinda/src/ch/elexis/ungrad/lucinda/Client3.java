package ch.elexis.ungrad.lucinda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class Client3 {
	private String api = "/lucinda/3.0";
	private Logger log=LoggerFactory.getLogger("Lucinda v3 client");
	private HttpURLConnection conn;
	private String addr;
	
	private String doSend(final String api_call) throws IOException {
		URL url=new URL(addr+api_call);	
		conn=(HttpURLConnection)url.openConnection();
		conn.setRequestProperty("method", "get");
		conn.setConnectTimeout(5000);
		int response=conn.getResponseCode();
		if(response==HttpURLConnection.HTTP_OK) {
			BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buffer=new StringBuffer();
			while((line=in.readLine())!=null) {
				buffer.append(line);
			}
			in.close();
			return buffer.toString();
		}else {
			throw new IOException("could not read "+api_call);
		}
		
				
	}
	public void connect(final String server_ip, final int port, final Handler handler) throws IOException{
		addr="http://"+server_ip+":"+port+"/";
		String ans=doSend("");
		if(ans.contains("Lucinda")) {
			handler.signal(make("status:connected"));
		}
				
	}
	/*
	 * syntactic sugar to create and initialize a JsonObject with a single call
	 */
	private JsonObject make(String... params) {
		JsonObject ret = new JsonObject();
		for (String param : params) {
			String[] p = param.split(":");
			ret.put(p[0], p[1]);
		}
		return ret;
	}

		
}
