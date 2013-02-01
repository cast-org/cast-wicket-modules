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
package org.cast.cwm.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class used by (future) highlighting controls and display forms.
 * 
 * @author jbrookover
 *
 */
public class HighlightService {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HighlightService.class);

	private static final String CHARACTERSPACER = ":";
	private static final String HIGHLIGHTINGSPACER = "#";
	
	/**
	 * This is the html ID that the client-side javascript uses to update
	 * page-wide controls.  Set your markup ID to this value if you hlde
	 * page-wide highlight controls.
	 */
	public static final String GLOBAL_CONTROL_ID = "globalHighlight";

	private static HighlightService instance = new HighlightService();
	
	public static HighlightService get() {
		return instance;
	}
	
	/**
	 * List of registered highlighters
	 */
	private Map<Character, HighlightType> highlighters = new HashMap<Character, HighlightType>();
	
	/**
	 * Register a highlighter for this application.  The character key will determine how
	 * the CSS styles and javascript function.  This method does not check to see
	 * if an appropriate style exists.
	 * 
	 * 
	 * @param c the style character, e.g. 'Y' tends to be for the color yellow
	 * @param label the name of this highlighter
	 */
	public void addHighlighter(Character c, String label, boolean editable) {
		Character key = Character.toUpperCase(c);

		if (key.equals(Character.valueOf('E')))
			throw new IllegalArgumentException("The character 'E' is reserved for the Erase function.");
		
		if (highlighters.containsKey(key))
			throw new IllegalArgumentException("The character " + c + " is already being used.");
		
		highlighters.put(key, new HighlightType(key, label, editable));
	}
	
	/**
	 * Get a higlighter associated with this character
	 * @param c
	 * @return the highlighter, or null if not found
	 */
	public HighlightType getHighlighter(Character c) {
		Character key = Character.toUpperCase(c);
		return highlighters.get(key);
	}
	
	public List<HighlightType> getHighlighters() {
		return new ArrayList<HighlightType>(highlighters.values());
	}

	/**
	 * Converts a list of color characters (R = Red, B = Blue, etc) and their corresponding
	 * highlighted words into a single, encoded string to be stored in the database.
	 * 
	 * @param colors a list of color characters
	 * @param highlights a list of highlighted words (default: CSV of word indexes)
	 * @return the encoded string.  
	 * 
	 * @see #decodeHighlights(String)
	 */
	public String encodeHighlights(List<Character> colors, List<String> highlights) {
		
		String s = "";
		
		if (colors.size() != highlights.size()) 
			throw new IllegalArgumentException("Argument arrays must have equal, positive length");
		
		if (colors.size() < 1)
			return s;
		
		for (int i = 0; i < colors.size(); i++) {
			s += colors.get(i) + CHARACTERSPACER + highlights.get(i) + HIGHLIGHTINGSPACER;
		}
		s = s.substring(0, s.lastIndexOf(HIGHLIGHTINGSPACER)); // Strip last spacer
		
		return s;
	}
	
	/**
	 * Converts an encoded highlighting string into a map of Color to CSV of words.  By
	 * default, the string is in the format:<br />
	 * <br />
	 * R:1,2,3#B:12,13,14,15#Y:22,23,24<br/>
	 * <br />
	 * In the above example, the first three words are highlighted with Red and the twelfth
	 * through fifteenth words are highlighted with Blue, etc.
	 * 
	 * @param s the encoded string
	 * @return the map of color letters to CSV defined highlighting.
	 * 
	 * @see #encodeHighlights(List, List)
	 */
	public Map<Character,String> decodeHighlights(String s) {
		
		Map<Character,String> highlights = new HashMap<Character,String>();

		List<String> colors = Arrays.asList(s.split(HIGHLIGHTINGSPACER));
		
		for (String colorString : colors) {
			String[] highlight = colorString.split(CHARACTERSPACER);
			if (highlight.length == 2 && highlight[0].length() == 1) {
				highlights.put(highlight[0].charAt(0), highlight[1]);
			}
		}
		
		return highlights;
	}
	
	/**
	 * Represents a single color highlighter for the application.  The default label is set
	 * application wide, but some labels will be overridden by user-stored labels when 
	 * displayed on the page.
	 * 
	 * @author jbrookover
	 *
	 */
	@Getter
	public static class HighlightType implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final Character color;
		private final String defaultLabel;
		private final boolean editable;
		
		public HighlightType (Character color, String defaultLabel, boolean editable) {
			this.color = color;
			this.defaultLabel = defaultLabel;
			this.editable = editable;
		}
	}
}
