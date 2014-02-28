/*
 * Copyright 2011-2014 CAST, Inc.
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import lombok.Getter;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.cast.cwm.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proof-of-concept Model that reads the GZipped-AU audio data from a Response
 * and makes it available as WAVE data, which is compatible with more audio players.
 *
 * For mp3 we'd need additional Java Sound plugins, eg tritonus
 */
public class ConvertedWavAudioModel extends LoadableDetachableModel<byte[]> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ConvertedWavAudioModel.class);

	@Getter
	private IModel<? extends Response> mResponse;
	
	/**
	 * Construct a model for the given Response.
	 * 
	 * @param response model of the response in the datastore
	 */
	public ConvertedWavAudioModel(IModel<? extends Response> response) {
		this.mResponse = response;
	}

	@Override
	protected byte[] load() {
		if (mResponse == null || mResponse.getObject() == null || mResponse.getObject().getResponseData() == null)
			return null;
		try {
			InputStream is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(mResponse.getObject().getResponseData().getBytes())));
			AudioInputStream ais = AudioSystem.getAudioInputStream(is);
			log.debug("Input format: " + ais.getFormat());
			// Set up stream as a buffer
			ByteArrayOutputStream waveStream = new ByteArrayOutputStream();
			// Fill it, with proper WAVE header
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, waveStream);
			return (waveStream.toByteArray());
			// TODO: should this be streamed somehow, eg via PipedOutputStream, rather than doing all conversion ahead of time?
			// conversion could be pushed to a separate thread.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		super.onDetach();
	}
}
