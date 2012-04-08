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
package org.cast.cwm.data.models;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.cast.audioapplet.component.IAudioAppletModel;
import org.cast.cwm.data.Response;


/**
 * <p>This is an implementation of {@link IAudioAppletModel} that is backed
 * by a {@link Response} in the data store.  Upon detach, this model will
 * drop all of its data and subsequent loads will look to the datastore.
 * 
 * <p>
 * <strong>Note</strong>: This model does not make changes to the datastore.  Any
 * calls to {@link #setObject(byte[])} will create a transient byte[] that must
 * be persisted within that request.  You an access the backing datastore response
 * via {@link #getMResponse()}.
 * </p>
 * 
 * @author jbrookover
 *
 */
public class LoadableDetachableAudioAppletModel extends LoadableDetachableModel<byte[]> implements IAudioAppletModel {

	private static final long serialVersionUID = 1L;
	
	
	
	@Getter
	private IModel<? extends Response> mResponse;
	
	@Getter @Setter
	private boolean readOnly;
	
	@Getter @Setter
	private int maxLength;

	/**
	 * Construct an unlimited length recording model backed by the provided
	 * response in the datastore.
	 * 
	 * @param response model of the response in the datastore
	 */
	public LoadableDetachableAudioAppletModel(IModel<? extends Response> response) {
		this(response, false, -1);
	}

	
	/**
	 * Construct an unlimited length recording model backed by the provided
	 * response in the datastore.
	 * 
	 * @param response model of the response in the datastore
	 * @param readOnly if true, calls to setObject() will throw an exception.
	 */
	public LoadableDetachableAudioAppletModel(IModel<? extends Response> response, boolean readOnly) {
		this(response, readOnly, -1);
	}

	/**
	 * Construct an limited length recording model backed by the provided
	 * response in the datastore.
	 * 
	 * @param response model of the response in the datastore
	 * @param readOnly if true, calls to setObject() will throw an exception.
	 * @param maxLength maximum allowed length of the recording, in seconds.
	 */
	public LoadableDetachableAudioAppletModel(IModel<? extends Response> response, boolean readOnly, int maxLength) {
		this.mResponse = response;
		this.readOnly = readOnly;
		this.maxLength = maxLength;
	}

	@Override
	public void setObject(byte[] audio) {
		if(readOnly) {
			throw new RuntimeException("Audio is read-only");
		}
		super.setObject(audio);
	}

	@Override
	protected byte[] load() {
		if (mResponse != null && mResponse.getObject() != null && mResponse.getObject().getResponseData() != null)
			return mResponse.getObject().getResponseData().getBytes();
		return null;
	}
	
	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		super.onDetach();
	}
}
