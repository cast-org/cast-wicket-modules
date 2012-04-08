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
package org.cast.cwm.data.component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.wicket.Resource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
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
public class WavResource extends Resource {
	
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
	public IResourceStream getResourceStream() {
		return new WavResourceStream();
	}

	
	private class WavResourceStream implements IResourceStream {
		
		private transient byte[] waveBytes;
		ByteArrayInputStream inputStream;
		
		private static final long serialVersionUID = 1L;

		public void close() throws IOException {
			if (inputStream != null)
				inputStream.close();
			inputStream = null;
		}

		public String getContentType() {
			return "audio/wave";
		}

		public InputStream getInputStream() throws ResourceStreamNotFoundException {
			convert();
			// TODO: make this a PipedOutputStream, where a separate thread fills it, rather than doing all conversion ahead of time.
			if (inputStream == null)
				inputStream = new ByteArrayInputStream(waveBytes);
			return inputStream;
		}
		
		private void convert() {
			if (waveBytes != null)
				return;  // already converted
			try {
				InputStream is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(mResponseData.getObject().getBytes())));
				// For mp3 we'd need additional Java Sound plugins, eg tritonus
				AudioInputStream ais = AudioSystem.getAudioInputStream(is);
				log.debug("Input format: " + ais.getFormat());
				// Set up stream as a buffer
				ByteArrayOutputStream waveStream = new ByteArrayOutputStream();
				// Fill it, with proper WAVE header
				AudioSystem.write(ais, AudioFileFormat.Type.WAVE, waveStream);
				waveBytes = waveStream.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		public Locale getLocale() {
			return null;
		}

		public long length() {
			convert();
			return waveBytes.length;
		}

		public void setLocale(Locale locale) {
		}

		public Time lastModifiedTime() {
			return Time.valueOf(mResponseData.getObject().getCreateDate());
		}
		
	}
	
}
