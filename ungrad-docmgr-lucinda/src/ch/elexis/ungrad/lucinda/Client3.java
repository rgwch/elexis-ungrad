package ch.elexis.ungrad.lucinda;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class Client3 {
	private String api = "/lucinda/3.0";
	private Logger log = LoggerFactory.getLogger("Lucinda v3 client");
	private HttpURLConnection conn;

	private URL makeURL(final String call) throws MalformedURLException {
		String server = Preferences.get(Preferences.SERVER_ADDR, "127.0.0.1"); //$NON-NLS-1$
		int port = Integer.parseInt(Preferences.get(Preferences.SERVER_PORT, "9997")); //$NON-NLS-1$
		return new URL("http://" + server + ":" + port + api+ call);

	}

	private byte[] doGet(final String api_call) throws IOException {
		URL url = makeURL(api_call);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("method", "get");
		conn.setConnectTimeout(5000);
		int response = conn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedInputStream bin = new BufferedInputStream(conn.getInputStream());
			int c;
			while ((c = bin.read()) != -1) {
				baos.write(c);
			}
			return baos.toByteArray();
		} else {
			throw new IOException("could not read " + api_call + ": Status was " + response);
		}
	}

	private String doPost(final String api_call, final String body) throws IOException {
		URL url = makeURL(api_call);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("method", "post");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
		// conn.setConnectTimeout(5000);
		conn.setDoOutput(true);

		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.write(body);
		os.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuffer buffer = new StringBuffer();
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		in.close();
		conn.disconnect();
		return buffer.toString();
	}

	/*
	 * public void connect(final String server_ip, final int port, final Handler
	 * handler) { try { byte[] ret=doGet(""); String ans=new String(ret,"utf-8");
	 * if(ans.contains("Lucinda")) { handler.signal(make("status:connected")); }else
	 * { handler.signal(make("status:failure")); } }catch(Exception ex) {
	 * ExHandler.handle(ex);
	 * handler.signal(make("status:failure","message:"+ex.getMessage())); }
	 * 
	 * }
	 */
	public void query(final String phrase, final Handler handler) {
		try {
			HashMap<String, Object> params = new HashMap<>();
			HashMap<String, Object> query = new HashMap<>();
			HashMap<String, Object> edismax = new HashMap<>();
			edismax.put("query", phrase);
			query.put("edismax", edismax);
			params.put("query", query);
			params.put("limit", Preferences.get(Preferences.MAXIMUM_HITS, "100"));
			String result = doPost("/query", writeJson(params));
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("result", readJson(result).get("docs"));
			json.put("status", "ok");
			handler.signal(json);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			handler.signal(make("status:error", "message:" + e.getMessage()));
		}
	}

	public void get(final String id, final Handler handler) {
		Map<String, Object> json = new HashMap<String, Object>();
		try {
			byte[] result = doGet("/get/" + id);
			json.put("status", "ok");
			json.put("result", result);
		} catch (Exception e) {
			ExHandler.handle(e);
			json.put("status", "error");
			json.put("message", e.getMessage());
		}
		handler.signal(json);
	}

	public void rescan(final Handler handler) {

	}

	public void addToIndex(final String id, final String title, final String doctype, Map metadata,
			final byte[] contents, final Handler handler) {

	}

	public void addFile(final String uid, final String filename, final String concern, final String doctype,
			Map<String, Object> metadata, final byte[] contents, final Handler handler) {

	}

	public void shutDown() {
	}

	private Map<String, Object> prepare(final String id, final String title, final String doctype, Map metadata,
			final byte[] contents) throws IOException {
		if (metadata == null) {
			metadata = new HashMap<String, Object>();
		}
		if (!StringTool.isNothing(id)) {
			metadata.put("id", id);
		}
		metadata.put("title", title);
		metadata.put("lucinda_doctype", doctype);
		metadata.put("filename", title);
		metadata.put("payload", contents);
		return metadata;
	}

	/*
	 * syntactic sugar to create and initialize a JsonObject with a single call
	 */
	private Map make(String... params) {
		Map<String, Object> ret = new HashMap<>();
		for (String param : params) {
			String[] p = param.split(":");
			ret.put(p[0], p[1]);
		}
		return ret;
	}

	public Map<String, Object> readJson(String source) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> res = mapper.readValue(source, new TypeReference<Map<String, Object>>() {
			});
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	public String writeJson(Map<String, Object> source) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(source);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}

	}

}
