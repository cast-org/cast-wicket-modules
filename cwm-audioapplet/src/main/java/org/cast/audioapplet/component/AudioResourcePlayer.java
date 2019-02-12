/*
 * Copyright 2011-2019 CAST, Inc.
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

import org.apache.wicket.IResourceListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.string.Strings;

/**
 * An instance of the audio player with audio content delivered as a Resource.
 */
public class AudioResourcePlayer extends AudioPlayer implements IResourceListener {

	private static final long serialVersionUID = 1L;
	
	protected IResource audioResource = null;
	
	public AudioResourcePlayer(String id, IResource audioResource, boolean autoPlay) {
		super(id, null, autoPlay);
		this.audioResource = audioResource;
	}
	
	@Override
	protected boolean hasData() {
		if (audioResource != null)
			return true;
		return super.hasData();
	}
	
	@Override
	protected CharSequence getDataUrl() {
		if (audioResource != null) {
			CharSequence url = urlFor(IResourceListener.INTERFACE, null);
			return RequestCycle.get().getOriginalResponse()
				.encodeURL(Strings.replaceAll(url, "&", "&amp;"));
		}
		return super.getDataUrl();
	}

	/**
	 * Called if we were initialized with a Resource and it's now being requested.
	 */
	public void onResourceRequested() {
		// TODO rewritten - does this work??
		getRequestCycle().scheduleRequestHandlerAfterCurrent(new ResourceRequestHandler(audioResource, null));
	}

}
