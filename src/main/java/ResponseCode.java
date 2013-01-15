import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class ResponseCode {
	public static String responseCode(String urlString) throws IOException {
		urlString = urlString.trim();
		if (!urlString.startsWith("http://")) {
			urlString = "http://" + urlString;
		}
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			return String.format("Malformed URL (%s)", urlString);
		}
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			return e.getClass().getName();
		}
		connection.setRequestMethod("HEAD");
		try { 
			connection.connect();
			int code = connection.getResponseCode();
			connection.disconnect();
			return String.valueOf(code);
		} catch (UnknownHostException e) {
			return String.format("Unknown host (%s)", urlString);
		} catch (SocketTimeoutException e) {
			return "Timeout";
		}
	}
	
	public static void main(String[] args) throws IOException {
		try {
			if (args[0].equals("pwd")) {
				pwd();
			} else 	if (args[0].equals("-f")) {
				File file = new File(args[1]);
				if (! file.exists() ) {
					System.out.format("File '%s' does not exist.%n", file);
					return;
				}
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					System.out.format("%s %s%n", line, responseCode(line));
				}
			} else {
				System.out.println(responseCode(args[0]));
			}
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Returns HTTP response code from HEAD request for provided address(es).");
			System.out.format("Usage:%n\t java %s address %n\t\t(to print response code)", ResponseCode.class.getCanonicalName());
			System.out.format("%n\t java %s -f file%n\t\t(to report response codes from addreses in file)", ResponseCode.class.getCanonicalName());
			//System.out.format("%n\t java %s pwd - prints current working directory", ResponseCode.class.getCanonicalName());
		}
	}
	
	private static void pwd() {
		File f = new File(".");
		try {
			System.out.println(f.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

