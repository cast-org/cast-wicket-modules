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


/*
public class AudioFileReceiveImpl extends AbstractAudioFileReceive {

	public AudioFileReceiveImpl (PageParameters parameters) {
		super(parameters);
	}

	@Override
	protected RecorderAppletTransmitObject getResponse(RecorderAppletTransmitObject message) {
		RecorderAppletTransmitObject response = null;
		
		if (message.getAction() == RecorderAppletTransmitObject.Action.SAVE) {
			IModel<Response> r;
			
			// Modify an existing response or create a new one.
			if (message.getDataStoreAudioId() != null && !message.getDataStoreAudioId().equals("")) {
				r = ResponseService.get().getResponseById(Long.valueOf(message.getDataStoreAudioId()));
			} else {
				IModel<Prompt> p = ResponseService.get().getPromptById(Long.valueOf(message.getDataStoreOtherId()));
				r = ResponseService.get().newResponse(CwmSession.get().getUserModel(), ResponseType.AUDIO, p);
			}
			
			ResponseService.get().saveBinaryResponse(r, message.getBytes(), "audio/au", null);

			if (r.getObject().isTransient()) // TODO: How about HibernateObjectModel's isBound() method?
				throw new IllegalStateException("Audio Save Failed");
			response =  new RecorderAppletTransmitObject(null, null);
			response.setSuccess(true);
			response.setDataStoreAudioId(r.getObject().getId().toString());
			return response;
			
		} else if (message.getAction() == RecorderAppletTransmitObject.Action.LOAD) {
			IModel<Response> audioFile = ResponseService.get().getResponseById(Long.valueOf(message.getDataStoreAudioId()));
			response = new RecorderAppletTransmitObject(audioFile.getObject().getResponseData().getBytes(), RecorderAppletTransmitObject.Action.LOAD);
			response.setSuccess(true);
			response.setDataStoreAudioId(audioFile.getObject().getId().toString());
			return response;
		} else {
			return response;
		}

	}
	
}
*/