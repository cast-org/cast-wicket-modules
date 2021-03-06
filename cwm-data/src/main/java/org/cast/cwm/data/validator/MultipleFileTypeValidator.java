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
package org.cast.cwm.data.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * Validate for file type.  Identical to {@link FileTypeValidator}, but this
 * supports a {@link Collection} of {@link FileUpload} objects.
 * 
 * TODO: Don't use client side type for validation.
 * TODO: activation.jar doesn't support latest mimetypes (see META-INF inside Jar)
 * 
 * @author jbrookover
 *
 */
public class MultipleFileTypeValidator extends AbstractValidator<Collection<FileUpload>>{

	private static final long serialVersionUID = 1L;
	private List<String> mimeTypes;
	
	public MultipleFileTypeValidator (String... mimeTypes) {
		super();
		this.mimeTypes = Arrays.asList(mimeTypes);
	}

	@Override
	protected void onValidate(IValidatable<Collection<FileUpload>> validatable) {
		for (FileUpload f : validatable.getValue()) {
			String uploadType = f.getContentType();
			if (uploadType == null || !mimeTypes.contains(uploadType))
				error(validatable);
		}
	}

	@Override
	protected Map<String, Object> variablesMap(IValidatable<Collection<FileUpload>> validatable) {
		final Map<String, Object> map = super.variablesMap(validatable);
		String types = "'" + Strings.join("', '", mimeTypes.toArray(new String[0])) + "'";
		map.put("allowed", types);
		return map;
	}

}
