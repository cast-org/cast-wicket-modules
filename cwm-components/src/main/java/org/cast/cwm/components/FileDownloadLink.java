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
package org.cast.cwm.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * A download link to a byte[] of binary data.  Use this to download files that have
 * been stored in the database.
 * 
 * @author jbrookover
 *
 */
public class FileDownloadLink extends Link<byte[]> implements IDetachable {

	private static final long serialVersionUID = 1L;
	private IModel<String> mMimeType;
	private IModel<String> mFileName;
	
	/**
	 * Construct a download link for the given model, 
	 * with all parameters as models so that they don't have to be dereferenced
	 * until the link is actually clicked.
	 * 
	 * @param wicketId
	 * @param mData model of the byte[] that the user will download
	 * @param mMimeType model of the MIME Type string
	 * @param mFileName model of the file name of the file.
	 */
	public FileDownloadLink (String wicketId, IModel<byte[]> mData, IModel<String> mMimeType, IModel<String> mFileName) {
		super(wicketId, mData);
		this.mMimeType = mMimeType;
		this.mFileName = mFileName;
		setOutputMarkupId(true);
	}
	
	/**
	 * Construct a download link for the given object.
	 * 
	 * @param id the wicket component id
	 * @param model the model of the byte[] that the user is downloading
	 * @param mimeType the mimeType of the binary data (e.g. "image/png")
	 * @param fileName the file name to display in the download link (e.g. "dog.png")
	 */
	public FileDownloadLink(String id, IModel<byte[]> model, String mimeType, String fileName) {
		this(id, model, Model.of(mimeType), Model.of(fileName));
	}

	@Override
	public void onClick() {

		AbstractResourceStream rs = new AbstractResourceStream() {

			private static final long serialVersionUID = 1L;

			private transient InputStream inputStream;

			public InputStream getInputStream()
					throws ResourceStreamNotFoundException {
				inputStream = new ByteArrayInputStream(getModelObject());
				return inputStream;
			}

			public void close() throws IOException {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}		
			}
			
			@Override
			public String getContentType() {
				return mMimeType.getObject();
			}

			@Override
			public Bytes length() {
				return Bytes.bytes(getModelObject().length);
			}
		};
		
		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rs);
		handler.setContentDisposition(ContentDisposition.ATTACHMENT)
			.setFileName(mFileName.getObject());
			
		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);

	}

	@Override
	protected void onDetach() {
		if (mFileName != null)
			mFileName.detach();
		if (mMimeType != null)
			mMimeType.detach();
		super.onDetach();
	}
	
	
}
