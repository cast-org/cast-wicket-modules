/*
 * Copyright 2011-2013 CAST, Inc.
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
package org.cast.cwm.data.component;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.resource.UploadedFileResourceReference;
import org.cast.cwm.drawtool.extension.UploadExtension;
import org.cast.cwm.service.IResponseService;

import com.google.inject.Inject;

public class SvgUploadExtensionImpl extends UploadExtension {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private IResponseService responseService;

	public SvgUploadExtensionImpl(IModel<Response> model) {
		super();
		setDefaultModel(model);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String processFile(FileUpload file) {
		IModel<BinaryFileData> mUploadedFile = responseService.attachBinaryResponse((IModel<Response>) getDefaultModel(), file);
		CharSequence url = getRequestCycle().urlFor(new UploadedFileResourceReference(), new PageParameters().add("id", mUploadedFile.getObject().getId()));
		return url.toString();
	}
}
