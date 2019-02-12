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
package org.cast.cwm.data.component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that will return the audio data of a ResponseData object, converted to WAV format.
 * 
 * @author bgoldowsky
 *
 */
public class WavResource extends AbstractResource {
	
	IModel<ResponseData> mResponseData;
	
	private static final Logger log = LoggerFactory.getLogger(WavResource.class);
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct from a Model of a ResponseData object.
	 * Note: Resources don't get a detach message, so the Model will not be detached by this class.
	 * @param mResponseData
	 */
	public WavResource (IModel<ResponseData> mResponseData) {
		this.mResponseData = mResponseData;
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		final ResourceResponse response = new ResourceResponse();
		response.setContentType("audio/wave");
		response.setLastModified(Time.valueOf(mResponseData.getObject().getCreateDate()));
		
		if (response.dataNeedsToBeWritten(attributes)) {
			response.setContentDisposition(ContentDisposition.INLINE);
			final byte[] audioData = getConverted();
			if (audioData == null) {
				response.setError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(final Attributes attributes) {
						attributes.getResponse().write(audioData);
					}
				});
			}
		}
		return response;
	}

	private byte[] getConverted() {
		try {
			InputStream is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(mResponseData.getObject().getBytes())));
			// For mp3 we'd need additional Java Sound plugins, eg tritonus
			AudioInputStream ais = AudioSystem.getAudioInputStream(is);
			log.debug("Input format: " + ais.getFormat());
			// Set up stream as a buffer
			ByteArrayOutputStream waveStream = new ByteArrayOutputStream();
			// Fill it, with proper WAVE header
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, waveStream);
			return waveStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		return null;
	}

}
