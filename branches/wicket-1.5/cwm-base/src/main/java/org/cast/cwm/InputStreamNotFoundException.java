package org.cast.cwm;

/**
 * Thrown if a requested input stream cannot be found.
 */
public class InputStreamNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public InputStreamNotFoundException(final Throwable cause) {
		super(cause);
	}

}
