/*
 * Copyright 2011-2014 CAST, Inc.
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
package org.cast.cwm.glossary;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cast.cwm.xml.XmlDocument;

/**
 * A service class for storing Glossary words in a data store.  Configure this
 * class to use your own implementation with {@link #setInstance(GlossaryService)}.
 * 
 * @author jbrookover
 *
 */
public class GlossaryService {
	
	public static GlossaryService instance = new GlossaryService();
	
	/** Class of object that this application uses for glossary entries */
	private Class<? extends IGlossaryEntry> entryClass = BaseEntry.class;
	
	/**
	 * Returns this implementation of the {@link #GlossaryService}.  
	 * 
	 * @return
	 */
	public static GlossaryService get() {
		return instance;
	}
	
	/**
	 * Create and return a new IGlossaryEntry of the entryClass registered with this service class.  
	 * By default this is a {@link BaseEntry} object, but applications can use their own class
	 * (which must implement IGlossaryEntry) by calling {@link GlossaryService.setEntryClass(class)}
	 * 
	 * This method will only succeed if entryClass implements IWriteableGlossaryEntry; if you 
	 * have unwritable glossary entries you need to construct them some other way.
	 * 
	 * @return a new, blank object implementing IGlossaryEntry
	 */
	public IWritableGlossaryEntry newEntry() {
		IGlossaryEntry e;
		try {
			e = entryClass.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException (ex);
		}
		return (IWritableGlossaryEntry) e;
	}
	
	/**
	 * Return a new IGlossaryEntry wrapped in an appropriate sort of Model.
	 * Applications that define an IGlossaryEntry class that is linked to a datastore
	 * will want to override this to use a detachable model, for instance.
	 * 
	 * This method will only succeed if entryClass implements IWriteableGlossaryEntry; if you 
	 * have unwritable glossary entries you need to construct them some other way.
	 * 
	 * @return properly model-wrapped blank glossary entry.
	 */
	public IModel<? extends IWritableGlossaryEntry> newEntryModel() {
		IWritableGlossaryEntry e = newEntry();
		return new Model<IWritableGlossaryEntry>(e);
	}
	
	/**
	 * Initialize a Glossary from an XML document.
	 * 
	 * @param doc XML document
	 * @return the new Glossary
	 */
	public Glossary parseXmlGlossaryDocument (XmlDocument doc) {
		Glossary g = new Glossary();
		doc.addObserver(new GlossaryXmlDocumentObserver(g), true); // will call the xmlUpdated method now and whenever the glossary changes.
		return g;
	}

	public Class<? extends IGlossaryEntry> getEntryClass() {
		return entryClass;
	}

	public void setEntryClass(Class<? extends IGlossaryEntry> entryClass) {
		this.entryClass = entryClass;
	}

}
