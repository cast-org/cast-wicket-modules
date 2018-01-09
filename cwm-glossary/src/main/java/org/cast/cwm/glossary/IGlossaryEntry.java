/*
 * Copyright 2011-2018 CAST, Inc.
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

import java.io.Serializable;
import java.util.Collection;

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
public interface IGlossaryEntry extends Serializable {

	/**
	 * Return the persistent unique identifier of this glossary entry.
	 * This will be used in URLs and the like.
	 *
	 * @return the ID as a String
	 */
	public String getIdentifier();
	
	/**
	 * Returns the citation form of the glossary word that this entry represents.
	 * This will be used for display in navigation and headers.
	 * @return
	 */
	public String getHeadword();

	/**
	 * Get the String by which this entry should be sorted.
	 * In many cases this is identical to the headword,
	 * but may differ for instance by being normalized to lowercase,
	 * having "a" or "the" removed from the beginning, or by putting 
	 * a person's surname first for purposes of sorting.
	 * 
	 * @return the sortForm of this entry
	 */
	public String getSortForm();

	/**
	 * Return a short (tooltip-style) definition for this word.
	 * Optional; if there is no short definition this may simply return null.
	 * @return a short definition, or null.
	 */
	public String getShortDef();
	
	/**
	 * Return the list of alternate forms of this word.
	 * These are the strings other than the entry name that would be linked to the glossary entry,
	 * such as plurals, abbreviations, or synonyms.
	 * 
	 * @return a collection of alternate forms, each one a string.
	 */
	public Collection<String> getAlternateForms();
	
	/**
	 * Return a pointer to the XML for the glossary item's definition.
	 * @return
	 */
	public ICacheableModel<? extends IXmlPointer> getXmlPointer();
	
}