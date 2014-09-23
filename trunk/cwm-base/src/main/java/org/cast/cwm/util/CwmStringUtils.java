package org.cast.cwm.util;

/**
 * Holds a few utililty methods for string manipulation.
 * 
 * @author bgoldowsky
 *
 */
public class CwmStringUtils {

	/**
	 * Replace any string of whitespace characters with a single space,
	 * and remove whitespace from the beginning and end of the string.
	 * 
	 * @return the new string.
	 */
	public static String normalizeWhitespace(String orig) {
		return orig.replaceAll("\\s+", " ").trim();
	}
	
}
