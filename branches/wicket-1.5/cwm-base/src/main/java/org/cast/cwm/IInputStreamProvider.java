package org.cast.cwm;

import java.io.InputStream;
import java.io.Serializable;

import org.apache.wicket.util.watch.IModifiable;

/**
 * An object that can provide an InputStream on request,
 * and an indication of when the data backing that stream was last modified.
 * 
 * Objects of this sort are serializable and can be passed and stored as data sources.
 */
public interface IInputStreamProvider extends IModifiable, Serializable {
	
	public abstract InputStream getInputStream() throws InputStreamNotFoundException;

}
