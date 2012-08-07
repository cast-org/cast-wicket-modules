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
package org.cast.cwm.data.validator;

import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * Validate for file type on a URL.
 * 
 * @author jbrookover
 *
 */
public class UrlTypeValidator extends AbstractValidator<String>{

	private static final long serialVersionUID = 1L;
	private List<String> mimeTypes;
	
	public UrlTypeValidator (String... mimeTypes) {
		super();
		this.mimeTypes = Arrays.asList(mimeTypes);
	}

	@Override
	protected void onValidate(IValidatable<String> validatable) {

		try {
			URL url = new URL(validatable.getValue());
			URLConnection c = url.openConnection();
			
			// Get Content Type
			// TODO: This just checks headers.  Better way?
			String contentType = c.getContentType();
			if (contentType == null || !mimeTypes.contains(contentType))
				error(validatable);
			
		} catch (Exception ex) {
			error(validatable); // Malformed URL or cannot connect
		}
	}
	
	@Override
	protected Map<String, Object> variablesMap(IValidatable<String> validatable) {
		final Map<String, Object> map = super.variablesMap(validatable);
	
		String type;
		try {
			URL url = new URL(validatable.getValue());
			
			// Ensure a connection can be made
			URLConnection c = url.openConnection();
			c.connect();
			
			type = c.getContentType();
			if (type == null)
				type = "unknown";
		
		} catch (Exception ex) {
			type = "unknown";
		}
		
		map.put("type", type);
		String types = "'" + Strings.join("', '", mimeTypes.toArray(new String[0])) + "'";
		map.put("allowed", types);
		return map;
	}

}
