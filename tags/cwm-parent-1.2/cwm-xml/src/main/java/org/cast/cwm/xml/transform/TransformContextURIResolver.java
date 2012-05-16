/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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