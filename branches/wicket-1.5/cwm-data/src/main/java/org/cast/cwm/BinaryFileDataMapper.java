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
package org.cast.cwm;

import java.util.List;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.request.resource.IResource;
import org.cast.cwm.data.resource.BinaryFileDataResource;

/**
 * This maps URLs like '/app/userdata/47' to the contents of the BinaryFileData with ID 47.
 *  
 * @author bgoldowsky
 *
 */
public class BinaryFileDataMapper extends AbstractMapper {

	protected final String urlPrefix;
	
	protected static final int COMPATIBILITY_SCORE = 10;
	
	public BinaryFileDataMapper (String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	public int getCompatibilityScore(Request request) {
		if(request.getUrl().toString().startsWith(urlPrefix))
			return COMPATIBILITY_SCORE;
		return 0;
	}

	public IRequestHandler mapRequest(Request request) {
		List<String> segments = request.getUrl().getSegments();
		if (segments.size() != 2)
			return null;
		if (!segments.get(0).equals(urlPrefix))
			return null;
		Long id = Long.valueOf(segments.get(1));
		return new ResourceRequestHandler(new BinaryFileDataResource(id), null);
	}

	public Url mapHandler(IRequestHandler requestHandler) {
		if (requestHandler instanceof ResourceRequestHandler) {
			ResourceRequestHandler rrh = (ResourceRequestHandler)requestHandler;
			IResource resource = rrh.getResource();
			if (resource instanceof BinaryFileDataResource) {
				BinaryFileDataResource bfdr = ((BinaryFileDataResource)resource);
				Url url = new Url();
				url.getSegments().add(urlPrefix);
				url.getSegments().add(bfdr.getId().toString());
				return url;
			}
		}
		
		return null;
	}

}
