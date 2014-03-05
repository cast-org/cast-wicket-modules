/*
 * Copyright 2011-2013 CAST, Inc.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * This class encapsulates the notion of a Glossary.
 * A glossary is a collection of entries (IGlossaryEntry objects).
 * Enough information about them is stored in maps so that the common 
 * operations (running GlossaryTransformer, making a navigation list 
 * of glossary terms, etc) can be completed without accessing
 * all of the individual glossary entries (which may require database queries, 
 * file access, etc).
 * 
 */
public class Glossary {

	@SuppressWarnings("unused")
	final private static Logger log = LoggerFactory.getLogger(GlossaryXmlDocumentObserver.class);
	
	/** Language of this glossary (in the future, use this to set alphabet, sorting rules, etc) */
	@Getter @Setter
	private String language = "en";

	/** Mapping of glossary entry IDs to (models of) the actual entries. */
	@Getter
	private Map<String,IModel<? extends IGlossaryEntry>> mapIdToModel = new HashMap<String,IModel<? extends IGlossaryEntry>>();

	/**
	 * A mapping of terms to glossary entry IDs.  For example, "gaseous", "gas state" and
	 * "gas" would all map to the id of the glossary entry "Gas".
	 */
	@Getter
	private Map<String, String> mapTermToId = new HashMap<String, String>();
	
	/**
	 * A mapping of characters of the alphabet to a sorted list of Entries that begin
	 * with that given letter. 
	 */
	@Getter
	private Map<Character, List<String>> mapFirstCharToIds = new HashMap<Character, List<String>>();
	
	/** Map of identifiers to short (tooltip-style) definitions */
	@Getter
	private Map<String, String> mapIdToShortDef = new HashMap<String, String>();
	
	final protected static LengthComparator glossTermComparator = new LengthComparator();
	
	/**
	 * Construct an empty Glossary object.
	 */
	public Glossary () {
	}
	
	/**
	 * Add a single entry to the glossary.
	 * Extracts all fields and places them into the proper maps.
	 * @param model of the IGlossaryEntry
	 */
	public void addEntry (IModel<? extends IGlossaryEntry> mEntry) {
		IGlossaryEntry entry = mEntry.getObject();
		String id = entry.getIdentifier();
		
		mapIdToModel.put(id, mEntry);
		
		for (String term : entry.getAlternateForms())
			mapTermToId.put(term, id);
		
		if (!Strings.isEmpty(entry.getShortDef()))
			mapIdToShortDef.put(id, entry.getShortDef());
		
		char firstChar = normalizeFirstChar(entry.getHeadword().charAt(0));
		if (!mapFirstCharToIds.containsKey(firstChar))
			mapFirstCharToIds.put(firstChar, new ArrayList<String>());
		if (!mapFirstCharToIds.get(firstChar).contains(id))
			mapFirstCharToIds.get(firstChar).add(id);
		Collections.sort(mapFirstCharToIds.get(firstChar), new GlossaryIdentifierComparator());
	}

	/**
	 * Fetch a single glossary entry from this glossary, keyed by its identifier.
	 * @param id
	 * @return the model wrapping the glossary entry
	 */
	public IModel<? extends IGlossaryEntry> getEntryById (String id) {
		return mapIdToModel.get(id);
	}
	
	/**
	 * Get the short definition of the entry with the given id.
	 * This does not attach the model of the entry so it can be efficiently used at any time.
	 * @param id
	 * @return the short definition as a string
	 */
	public String getShortDefById (String id) {
		return mapIdToShortDef.get(id);
	}
	
	/**
	 * Fetch a glossary entry given one of its alternate forms.
	 * @param term
	 * @return
	 */
	public IModel<? extends IGlossaryEntry> getEntryByForm (String term) {
		String id = mapTermToId.get(term);
		if (id != null)
			return mapIdToModel.get(id);
		return null;
	}
	
	/**
	 * Get a list of ids of all glossary entries that start with the given letter.
	 * @param firstChar
	 * @return a list of strings, which are the IDs of glossary entries.  Guaranteed not null.
	 */
	public List<String> getEntryIdsByFirstChar (char firstChar) {
		List<String> list = mapFirstCharToIds.get(normalizeFirstChar(firstChar));
		if (list != null)
			return list;
		else
			return Collections.emptyList();
	}
	
	/**
	 * Detach any detachable models that the Glossary is holding.
	 */
	public void detach() {
		for (IModel<? extends IGlossaryEntry> m : mapIdToModel.values())
			if (m instanceof IDetachable)
				m.detach();
	}

	/**
	 * Normalization function used by this glossary for headwords.
	 * Default implementation just cleans up whitespace.
	 * 
	 * @param extracted headword
	 * @return headword that will actually be used.
	 */
	public String normalizeHeadword (String string) {
		return string.replaceAll("\\s+", " ").trim();
	}
	
	/**
	 * Normalization function used by this glossary for terms.
	 * Default implementation forces lowercase, trims and normalizes whitespace.
	 * @param string the term to be normalized
	 * @return normalized version of the term
	 */
	public String normalizeTerm(String string) {
		return (string.replaceAll("\\s+", " ").trim().toLowerCase());
	}
	
	/**
	 * Normalization function used by this Glossary for the first letters of words.
	 * Default implementation forces the character to uppercase.
	 * 
	 * @param a character
	 * @return the normalized form of that character.
	 */
	public char normalizeFirstChar (char firstChar) {
		return Character.toUpperCase(firstChar);
	}
	
	/**
	 * Comparator that sorts IGlossaryEntry objects by their sortForms.
	 */
	public class GlossaryEntryComparator implements Comparator<IGlossaryEntry>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * @param e1 entry 1
		 * @param e2 entry 2
		 * @return less than, equal to, or greater than 0 depending on e1<=>e2
		 * @throws NullPointerException if either <code>e1</code> or
		 *             <code>e2</code> is null
		 */
		public int compare (IGlossaryEntry e1, IGlossaryEntry e2) {
			return normalizeTerm(e1.getSortForm()).compareTo(normalizeTerm(e2.getSortForm()));
		}
	}
	
	/**
	 * Comparator that sorts IDs of glossary entries (which are Strings) by the sort forms of the corresponding entries. 
	 */
	public class GlossaryIdentifierComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * @param id1 ID of entry 1
		 * @param id2 ID of entry 2
		 * @return less than, equal to, or greater than 0 depending on e1<=>e2
		 * @throws NullPointerException if either <code>e1</code> or
		 *             <code>e2</code> is null
		 */
		public int compare (String id1, String id2) {
			 return getEntryById(id1).getObject().getSortForm().compareTo(getEntryById(id2).getObject().getSortForm());
		}
		
	}
		
	/**
	 * Comparator for alternate forms of glossary words.
	 * The map of glossary terms is returned in longest-first order, which is useful for 
	 * finding matches since full terms should be marked in preference to shorter terms
 	 * that may be found within them.
	 */
	protected static class LengthComparator implements Comparator<String> {
		public int compare(String a, String b) {
			if (a.length() < b.length())
				return (1);
			else if (a.length() > b.length())
				return -1;
			else
				return a.compareTo(b);
		}
	}
}

