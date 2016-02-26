/*
 * Copyright 2011-2016 CAST, Inc.
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
 * Service class used by highlighting controls and display forms.
 * 
 * @author jbrookover
 *
 */
public class HighlightService implements IHighlightService {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HighlightService.class);

	private static final String CHARACTERSPACER = ":";
	private static final String HIGHLIGHTINGSPACER = "#";
	
	private static IHighlightService instance = new HighlightService();
	
	public static IHighlightService get() {
		return instance;
	}
	
	/**
	 * List of registered highlighters
	 */
	private Map<Character, HighlightType> highlighters = new HashMap<Character, HighlightType>();
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IHighlightService#addHighlighter(java.lang.Character, boolean, boolean)
	 */
	@Override
	public void addHighlighter(Character c, boolean isOn, boolean editable) {
		Character key = Character.toUpperCase(c);

		if (key.equals(Character.valueOf('E')))
			throw new IllegalArgumentException("The character 'E' is reserved for the Erase function.");
		
		if (highlighters.containsKey(key))
			throw new IllegalArgumentException("The character " + c + " is already being used.");
		
		highlighters.put(key, new HighlightType(key, isOn, editable));
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IHighlightService#getHighlighter(java.lang.Character)
	 */
	@Override
	public HighlightType getHighlighter(Character c) {
		Character key = Character.toUpperCase(c);
		return highlighters.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IHighlightService#getHighlighters()
	 */
	@Override
	public List<HighlightType> getHighlighters() {
		return new ArrayList<HighlightType>(highlighters.values());
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IHighlightService#encodeHighlights(java.util.List, java.util.List)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IHighlightService#decodeHighlights(java.lang.String)
	 */
	@Override
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
		private final boolean isOn;
		private final boolean editable;
		
		public HighlightType (Character color, boolean isOn, boolean editable) {
			this.color = color;
			this.isOn = isOn;
			this.editable = editable;
		}
	}
}
