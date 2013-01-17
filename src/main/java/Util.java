

public class Util {
	public static String correctAddress(String urlString) {
		urlString = urlString.trim();
		if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
			return "http://" + urlString;
		}
		return urlString;
	}
}
