package org.cast.cwm.xml;

import java.io.InputStream;
import java.io.Serializable;

import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.watch.IModifiable;

/**
 * Interface for something that can provide XML content, like a file or a database.
 * 
 * @author bgoldowsky
 *
 */
public interface IXmlDocumentSource extends IModifiable, Serializable {
	
	public InputStream getInputStream() throws ResourceStreamNotFoundException;
	
}
