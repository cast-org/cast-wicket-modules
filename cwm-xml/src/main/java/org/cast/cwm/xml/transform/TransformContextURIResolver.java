package org.cast.cwm.xml.transform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.wicket.protocol.http.RequestUtils;
import org.cast.cwm.xml.service.XmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resolves the path for files referenced in xslt include or import statements.
 * XmlService stores the path information.
 * 
 * @author lynnmccormack
 *
 */
public class TransformContextURIResolver implements URIResolver {

	private static final Logger log = LoggerFactory.getLogger(TransformContextURIResolver.class);

	public Source resolve(String href, String base) throws TransformerException {
		String hrefPath = null;
		File file;
		
		// loop through the directories defined in xmlService to find the transformation file
		for (String directory : XmlService.get().getTransformerDirectories()) {
			file = new File(directory, href);
			if (file.exists()) {
				// return a valid stream
				return getValidStream(file);
			}
		}
		
		// If the file wasn't found in the explicit directories, check relative to the current transformation directory. 	
		hrefPath = RequestUtils.toAbsolutePath(base, href);	
		URL url = null;
		try {
			url = new URI(hrefPath).toURL();
		} catch (MalformedURLException e) {
			log.error("Error Malformed URL Exception for path {}", hrefPath);
			e.printStackTrace();
		} catch (URISyntaxException e) {
			log.error("Error URI Syntax Exception retrieving url for path {}", hrefPath);
			e.printStackTrace();
		}
		file = new File(url.getFile());
		if (file.exists()) {
			// return a valid stream
			return getValidStream(file);
		} 
		
		log.error("The xsl file {} was not found", href);
		return null;
	}
	
	/**
	 * Returns a StreamSource for a file that exists.
	 * @param file
	 * @return
	 */
	protected StreamSource getValidStream (File file) {
		try {
			return new StreamSource(file);
			} catch (Throwable t) {
				log.error("Error opening transformation file {}", file);
			}
		return null;
	}
}