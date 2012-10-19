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
package org.cast.audioapplet.component;

import org.apache.wicket.Resource;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * NOT WORKING YET.
 * 
 * This will be a minimal (eg, one-button) audio player that accepts an audio Resource to play.
 *  
 * @author borisgoldowsky
 *
 */
public class TinyAudioPlayer extends AudioResourcePlayer {

	private static final long serialVersionUID = 1L;

	public TinyAudioPlayer(String id, Resource audioResource, boolean autoPlay) {
		super(id, audioResource, autoPlay);
	}

	@Override
	protected void addButtons() {
		WebMarkupContainer play = new WebMarkupContainer("playButton");
		play.add(new SimpleAttributeModifier("onclick", "audioPlay('" + dj.getMarkupId() + "')"));
		add(play);
		
		WebMarkupContainer stop = new WebMarkupContainer("stopButton");
		stop.add(new SimpleAttributeModifier("onclick", "audioStop('" + dj.getMarkupId() + "')"));
		add(stop);
	}


}
