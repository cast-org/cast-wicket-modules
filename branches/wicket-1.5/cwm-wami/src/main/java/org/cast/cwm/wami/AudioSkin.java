package org.cast.cwm.wami;

import lombok.Getter;

/**
 * Identifiers for the implemented skins for the audio applet.
 * These may include Javascript and CSS, or just Javascript, allowing the 
 * application to supply its own CSS design.
 * 
 * At the moment there are two different modes of operation: a 3-button version 
 * (record, play/pause toggle, and stop) and a 4-button version (record, play, pause,
 * and stop).  Each has separate Javascript.
 * 
 * CSS for a standard 4-button look is supplied here.  There's no 3-button CSS currently
 * in this project (see the cet-hub project for examples).
 * 
 * @author bgoldowsky
 *
 */
public enum AudioSkin {

	THREE_BUTTON ("3button", false),
	STANDARD ("std", true);
	
	@Getter
	private String variationName;
	
	@Getter
	private boolean hasCss;

	private AudioSkin(String variationName, boolean hasCss) {
		this.variationName = variationName;
		this.hasCss = hasCss;
	}
	
}
