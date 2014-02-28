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

import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;

/**
 * An interface for a glossary entry representing a single glossary word.
 * Required fields are an ID, the headword (citation form) and a sortForm (used 
 * for finding this entry's place in the sorted glossary), and a boolean value
 * indicating whether the entry is current or obsolete (aka effectively deleted).
 *  
 * In addition there can be zero or more alternate forms of the word that are also
 * linked to it.
 */
public interface IWritableGlossaryEntry extends IGlossaryEntry {

	/**
	 * Set the persistent unique identifier of this glossary entry.
	 */
	public void setIdentifier(String identifier);
	
	/**
	 * Set the glossary word that this entry represents.
	 * 
	 * @param name
	 */
	public void setHeadword(String name);
	
	/**
	 * Set the sortForm of this entry.
	 * 
	 * @param sortForm
	 */
	public void setSortForm(String sortForm);
	
	/**
	 * Set the short definition of the entry.
	 * 
	 * @param shortDef
	 */
	public void setShortDef(String shortDef);
	
	/**
	 * Set the list of alternate forms for this entry.
	 * 
	 * @param collection of forms
	 */
	public void addAlternateForm(String form);
	
	/**
	 * Set the pointer to the XML for the glossary item's definition.
	 */
	public void setXmlPointer(ICacheableModel<? extends IXmlPointer> pointer);
	
}