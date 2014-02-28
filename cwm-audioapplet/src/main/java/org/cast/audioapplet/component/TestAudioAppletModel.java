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
package org.cast.audioapplet.component;


/**
 * <p>This is a simple implementation of {@link IAudioAppletModel}.  It stores
 * all data in a serialized field and, therefore, can drastically increase
 * the session size.</p>
 * 
 * <p>
 * <strong>Note</strong>: This is only recommended for testing purposes.  Do
 * not use in production.
 * </p>
 * 
 * @author jbrookover
 *
 */
public class TestAudioAppletModel implements IAudioAppletModel {

	private static final long serialVersionUID = 1L;
	private byte[] audio;
	private boolean readOnly;
	private int maxLength;


	/** Construct a new empty audio applet with unlimited recording length
	 * 
	 */
	public TestAudioAppletModel() {
		this(null, false, -1);
	}

	/** Construct the model for an audio applet component with unlimited recording length
	 * 
	 * @param audio a byte[] of audio to be preloaded (the initial file to load) if no file
	 * is available, use null or an empty byte[]
	 * @param readOnly if true, the applet will not allow recording of new data
	 */
	public TestAudioAppletModel(byte[] audio, boolean readOnly) {
		this(audio, readOnly, -1);
	}

	/** Construct the model for an audio applet component.
	 * 
	 * @param audio a byte[] of audio to be preloaded (the initial file to load) if no file
	 * is available, use null or an empty byte[]
	 * @param readOnly if true, the applet will not allow recording of new data
	 * @param maxLength the maximum length of the audio that will be allowed in a recording, in seconds
	 */
	public TestAudioAppletModel(byte[] audio, boolean readOnly, int maxLength) {
		this.audio = audio;
		this.readOnly = readOnly;
		this.maxLength = maxLength;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public byte[] getObject() {
		return audio;
	}

	@Override
	public int getMaxLength() {
		return maxLength;
	}

	@Override
	public void setObject(byte[] audio) {
		if(readOnly) {
			throw new RuntimeException("Audio is read-only");
		}
		else this.audio = audio;
	}

	@Override
	public void detach() {

	}


}
