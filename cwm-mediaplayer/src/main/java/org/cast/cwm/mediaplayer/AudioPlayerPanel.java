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
package org.cast.cwm.mediaplayer;

import org.apache.wicket.request.resource.PackageResourceReference;

/** Media player panel, initialized with parameters appropriate for playing audio.
 * 
 * @author Boris Goldowsky
 *
 */
public class AudioPlayerPanel extends MediaPlayerPanel {

	private static final long serialVersionUID = 1L;

	public AudioPlayerPanel(String id, String audioHRef, int width, int height) {
		super(id, audioHRef, width, height);
		this.fullScreen = false;
		this.showDownloadLink = true;
		this.fallbackText = "Click to play audio";
		this.downloadText = "Download MP3";
	}
	
	public AudioPlayerPanel(String id, PackageResourceReference resourceRef, int width, int height) {
	  super(id, resourceRef, width, height);
	  this.fullScreen = false;
	  this.showDownloadLink = true;
	  this.fallbackText = "Click to play audio";
	  this.downloadText = "Download MP3";
	}

}
