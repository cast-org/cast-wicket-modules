/*
 * Copyright 2011-2020 CAST, Inc.
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

import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

/**
 * Validate for file type on a URL.
 * 
 * @author jbrookover
 *
 */
public class UrlTypeValidator implements IValidator<String> {

	private List<String> mimeTypes;
	
	public UrlTypeValidator (String... mimeTypes) {
		super();
		this.mimeTypes = Arrays.asList(mimeTypes);
	}

	@Override
	public void validate(IValidatable<String> validatable) {

		try {
			URL url = new URL(validatable.getValue());
			URLConnection c = url.openConnection();
			c.connect();

			// Get Content Type
			// TODO: This just checks headers.  Better way?
			String contentType = c.getContentType();
			if (contentType == null || !mimeTypes.contains(contentType)) {
				ValidationError error = new ValidationError(this);

				error.setVariable("type", (contentType == null) ? "unknown" : contentType);

				String types = "'" + Strings.join("', '", mimeTypes.toArray(new String[0])) + "'";
				error.setVariable("allowed", types);

				validatable.error(error);
			}
		} catch (Exception ex) {
			// URL is malformed, or connection cannot be made
			validatable.error(new ValidationError(this));
		}
	}

}
