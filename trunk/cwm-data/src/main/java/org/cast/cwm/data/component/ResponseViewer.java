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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import lombok.Getter;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.cast.audioapplet.component.AudioPlayer;
import org.cast.cwm.components.FileDownloadLink;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.behavior.ChromeFrameUtils;
import org.cast.cwm.data.models.LoadableDetachableAudioAppletModel;
import org.cast.cwm.service.ImageService;

/**
 * A simple panel for viewing a response.
 * 
 * @author jbrookover
 * @see Response
 *
 */
public class ResponseViewer extends Panel {

	private static final long serialVersionUID = 1L;
	
	@Getter
	private final Integer maxWidth;  // marked final because info is used in constructor
	
	@Getter
	private final Integer maxHeight;  // marked final because info is used in constructor

	@Getter
	private PropertyModel<String> mResponseName;

	@Getter
	private IModel<? extends Response> mResponse;

	
	@Override 
	public void onDetach() {
		super.onDetach();
		
		if (mResponseName != null) {
			mResponseName.detach();
		}	
		if (mResponse != null) {
			mResponse.detach();
		}
	}
	
	public ResponseViewer(String id, final IModel<? extends Response> model) {
		this(id, model, null, null);
	}
	
	/**
	 * Create a panel to view the provided Response.
	 * 
	 * @param id
	 * @param model
	 */
	public ResponseViewer(String id, final IModel<? extends Response> model, Integer maxWidth, Integer maxHeight) {
		super(id, model);
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		setOutputMarkupId(true);
		this.mResponse = model;
		
		// Show default "No Response," if necessary.
		if (model == null
                || model.getObject() == null 
				|| !model.getObject().isValid() 
				|| model.getObject().getResponseData() == null) {
			add(getNoResponseComponent("response"));
			return;
		}

		// Show appropriate fragment depending on ResponseType
		switch (model.getObject().getType()) {
			case TEXT:
			case HTML:
				add(new TextFragment("response", model));
				break;
			case AUDIO:
				add(new AudioFragment("response", model));
				break;
			case UPLOAD:
				add(new UploadFragment("response", model));
				break;
			case SVG:
				add(new DrawingFragment("response", model));
				break;
			default:
				add(new Label("response", "[[Cannot Display Response Type: " + model.getObject().getType() + "]]"));
		}
	}
	
	public class TextFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		
		public TextFragment(String id, IModel<? extends Response> model) {
			super(id, "textFragment", ResponseViewer.this, model);
			add(new Label("text", new PropertyModel<String>(model, "text")).setEscapeModelStrings(false));
		}
	}
	
	public class DrawingFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		
		public DrawingFragment(String id, IModel<? extends Response> model) {
			super(id, "drawingFragment", ResponseViewer.this, model);
			add(new SvgImage("drawing", model, maxWidth, maxHeight));
		}
		
		@Override
		protected void onBeforeRender() {
			if (ChromeFrameUtils.isBareIE())
				replaceWith(new SvgNotSupportedFragment(this.getId()));
			super.onBeforeRender();
		}
	}
	
	public class AudioFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		
		public AudioFragment(String id, IModel<? extends Response> model) {
			super(id, "audioFragment", ResponseViewer.this, model);

			LoadableDetachableAudioAppletModel audioModel = new LoadableDetachableAudioAppletModel(model);
			audioModel.setReadOnly(true);
						
			AudioPlayer audioApplet = new AudioPlayer("applet", audioModel);
			add(audioApplet);
		}	
	}
	
	public class UploadFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		@Override public void onBeforeRender() {

			//Download Link
			FileDownloadLink download = new FileDownloadLink("download", new PropertyModel<byte[]>(getModel().getObject().getResponseData().getBinaryFileData(), "data"), 
					new Model<String>(getModel().getObject().getResponseData().getBinaryFileData().getMimeType()), mResponseName);

			
			download.add(new Label("filename", mResponseName));
			this.replace(download);

			//Displayed Image			
			if (getModel().getObject().getResponseData().getBinaryFileData().getPrimaryType().equals("image")) {
				Image displayImage = ImageService.get().getScaledImageComponent("imageDisplay", new PropertyModel<Long>(mResponse, "responseData.binaryFileData.id").getObject(), maxWidth, maxHeight);
				this.replace(displayImage);					
				super.onBeforeRender();				
			}
			else {
				EmptyPanel displayImage = new EmptyPanel("imageDisplay");
				this.replace(displayImage);					
				super.onBeforeRender();
			}			
			
			
		}

					
		public UploadFragment(String id, final IModel<? extends Response> model) {

			super(id, "uploadFragment", ResponseViewer.this, model);			
			mResponseName = new PropertyModel<String>(model, "responseData.binaryFileData.name");
			add(new Label("filename", mResponseName));

			//during runtime this is replaced in onBeforeRender			
			add(new EmptyPanel("download"));
			add(new EmptyPanel("imageDisplay"));
		
		}
		
	}		

	
	protected class SvgNotSupportedFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public SvgNotSupportedFragment(String id) {
			super(id, "svgNotSupportedFragment", ResponseViewer.this);
			
		}
	}
	
	/**
	 * Returns the component to display if this {@link ResponseViewer} attempts to
	 * display a null {@link Response} object.  Override this method to provide a custom
	 * display.
	 * 
	 * @param id wicket:id of the component
	 * @return
	 */
	protected Component getNoResponseComponent(String id) {
		return new Label(id, "<i>No Response to Display</i>").setEscapeModelStrings(false);
	}
	
	@SuppressWarnings("unchecked")
	public IModel<Response> getModel() {
		return (IModel<Response>) getDefaultModel();
	}
	
	public Response getModelObject() {
		return getModel().getObject();
	}

}
	
