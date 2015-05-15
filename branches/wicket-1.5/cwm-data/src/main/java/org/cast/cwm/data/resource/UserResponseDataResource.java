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
package org.cast.cwm.data.resource;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.Response;
import org.cast.cwm.service.ICwmService;

import com.google.inject.Inject;

/**
 * A simple resource that requires a response "id" and serves up
 * the text from the response in the db as a file with the matching ID.  
 * 
 * For example the url "myApplication/userresponse/123" would retrieve
 * the value of the text field for the response with id = 123
 * 
 * If the response and/or text value is not found, this will throw a 404 Not Found Error.
 * 
 * TODO: used for json files only - need to make generic for other types
 * of UserResponseData - ldm
 * 
 */
public class UserResponseDataResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Getter
	protected Long id;
	
	@Setter
	protected String contentType = "application/json";
	
	@Setter
	protected String textEncoding = "UTF-8";
	
	@Inject
	private ICwmService cwmService;
	
	private transient Response cwmResponse;
	
	public UserResponseDataResource(Long id) {
		super();
		this.id = id;
		Injector.get().inject(this);
	}

	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes) {
		ResourceResponse response = new ResourceResponse();

		cwmResponse = cwmService.getById(Response.class, id).getObject();

		// if there is no user response, return an error
		if (cwmResponse == null) {
    		throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Data not found [id=" + id + "]");
		}
		
		response.setLastModified(Time.valueOf(cwmResponse.getLastUpdated()));
		
		if (response.dataNeedsToBeWritten(attributes)) {
			response.setContentDisposition(ContentDisposition.INLINE);
			if (cwmResponse.getText() == null) {
				response.setError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(final Attributes attributes) {
						attributes.getResponse().write(cwmResponse.getText());
					}
				});
				response.setContentType(contentType);					
				response.setTextEncoding(textEncoding);
			}
		}
		return response;
	}
}