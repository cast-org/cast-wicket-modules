/*
 * Copyright 2011-2015 CAST, Inc.
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
