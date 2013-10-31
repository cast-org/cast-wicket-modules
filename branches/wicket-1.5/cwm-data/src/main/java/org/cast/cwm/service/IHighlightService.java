package org.cast.cwm.service;

import java.util.List;
import java.util.Map;

import org.cast.cwm.service.HighlightService.HighlightType;

public interface IHighlightService {

	/**
	 * This is the html ID that the client-side javascript uses to update
	 * page-wide controls.  Set your markup ID to this value if you hlde
	 * page-wide highlight controls.
	 */
	public static final String GLOBAL_CONTROL_ID = "globalHighlight";

	/**
	 * Register a highlighter for this application.  The character key will determine how
	 * the CSS styles and javascript function.  This method does not check to see
	 * if an appropriate style exists.
	 * 
	 * 
	 * @param c the style character, e.g. 'Y' tends to be for the color yellow
	 * @param isOn true if the highlighter is enabled by the application config
	 * @param editable true if the highlighter name should be editable by the user
	 */
	public abstract void addHighlighter(Character c, boolean isOn,
			boolean editable);

	/**
	 * Get a higlighter associated with this character
	 * @param c
	 * @return the highlighter, or null if not found
	 */
	public abstract HighlightType getHighlighter(Character c);

	public abstract List<HighlightType> getHighlighters();

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
	public abstract String encodeHighlights(List<Character> colors,
			List<String> highlights);

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
	public abstract Map<Character, String> decodeHighlights(String s);

}