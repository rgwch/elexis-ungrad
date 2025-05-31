package ch.elexis.ungrad;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http {
	private Logger log = LoggerFactory.getLogger("Lucinda v3 client");

	public URL makeURL(final String server, final int port, final String call) throws MalformedURLException{
		return new URL("http://" + server + ":" + port + call);

	}
	public byte[] doGet(final URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			throw new IOException("could not read " + url.toString() + ": Status was " + response);
		}
	}

	public String doPost(final URL url, final String body, final int expectedStatus) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("method", "post");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
		// conn.setConnectTimeout(5000);
		conn.setDoOutput(true);

		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.write(body);
		os.flush();
		int response = conn.getResponseCode();
		if (response == expectedStatus) {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			in.close();
			conn.disconnect();
			return buffer.toString();
		} else {
			log.error("Bad answer for " + url.toString() + ": " + response);
			return null;
		}
	}

}
