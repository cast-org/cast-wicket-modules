package org.cast.cwm.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lynnmccormack
 * Stream the contents of a URL post to a string.
 */
public class UrlStreamedToString {

	protected URL url;
	private static final Logger log = LoggerFactory
			.getLogger(UrlStreamedToString.class);

	public UrlStreamedToString(URL url) {
		this.url = url;
	}

	/**
	 * @return the string of data streamed from the URL post
	 */
	public String getPostString() {
		StringBuffer stringBuffer = new StringBuffer();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch (IOException e) {
			log.error("There is a problem opening the url {}", url);
			e.printStackTrace();
		}

		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null)
				stringBuffer.append(inputLine);
			in.close();
		} catch (IOException e) {
			log.error("There is a problem reading the url {}", url);
			e.printStackTrace();
		}

		return stringBuffer.toString();
	}

}
