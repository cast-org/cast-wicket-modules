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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.cast.audioapplet.component.AudioPlayer;
import org.cast.cwm.components.FileDownloadLink;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.models.LoadableDetachableAudioAppletModel;
import org.cast.cwm.data.resource.ThumbnailUploadedImageResourceReference;
import org.cast.cwm.data.resource.UploadedFileResourceReference;

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

	@Getter @Setter
	private Boolean displayTitle = true;  // marked final because info is used in constructor
	
	@Getter @Setter
	private Boolean displayDownloadLink = true;  // marked final because info is used in constructor

	
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
		add(getViewerFragment("response", model, model.getObject().getType()));
		
	}

	protected Component getViewerFragment(String id, IModel<? extends Response> model, IResponseType type) {
		String typeName = type.getName();
		if (typeName.equals("TEXT"))
			return (new TextFragment(id, model));
		if (typeName.equals("HTML"))
			return (new TextFragment(id, model));
		if (typeName.equals("AUDIO"))
			return (new AudioFragment(id, model));
		if (typeName.equals("UPLOAD"))
			return (new UploadFragment(id, model));
		if (typeName.equals("SVG"))
			return (new DrawingFragment(id, model));
		if (typeName.equals("TABLE"))
			return (new TableFragment(id, model));
		return new Label(id, "[[Cannot Display Response Type: " + typeName + "]]");
	}

	public class TextFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		
		public TextFragment(String id, IModel<? extends Response> model) {
			super(id, "textFragment", ResponseViewer.this, model);
			add(new Label("text", new PropertyModel<String>(model, "text")).setEscapeModelStrings(false));
		}
	}

	public class TableFragment extends Fragment implements IHeaderContributor {
		private static final long serialVersionUID = 1L;
		protected String divMarkupId; // used by the js
		protected CharSequence tableUrl;
		
		public TableFragment(String id, IModel<? extends Response> model) {
			super(id, "tableFragment", ResponseViewer.this, model);

			// add the div for the table so that we can uniquely identify this table by its markup id
			WebMarkupContainer tableContainer = new WebMarkupContainer("gridDiv");
			add (tableContainer);
			tableContainer.setOutputMarkupId(true);
			divMarkupId = tableContainer.getMarkupId();
		}

		// The URL from which the table data can be loaded.
		protected CharSequence getDataUrl() {
			TableDataLoadAjaxBehavior loadBehavior = new TableDataLoadAjaxBehavior();
			ResponseViewer.TableFragment.this.add(loadBehavior);
			return (loadBehavior.getCallbackUrl());				
		}

		@Override
		public void onBeforeRender() {
			tableUrl = getDataUrl();
			super.onBeforeRender();
		}

		public void renderHead(IHeaderResponse response) {
			response.renderJavaScriptReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid.js"));
			response.renderJavaScriptReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid_renderers.js"));
			response.renderJavaScriptReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid_editors.js")); // tabbing
			response.renderJavaScriptReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid_utils.js"));
			response.renderJavaScriptReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid_cast.js"));

			response.renderCSSReference(new PackageResourceReference(ResponseEditor.class, "editablegrid/editablegrid.css"));

			// once the text for the grid is available in the hidden text field make this js call 
			String jsString = new String("cwmImportGrid(" + "\'" + divMarkupId + "\', \'"   + tableUrl + "\', \"true\");");
			response.renderOnDomReadyJavaScript(jsString);			
		}
	}

	
	// this behavior enables the data stored in the database to be streamed via a url
	class TableDataLoadAjaxBehavior extends AbstractAjaxBehavior {
		private static final long serialVersionUID = 1L;

		public void onRequest() {
			getRequestCycle().scheduleRequestHandlerAfterCurrent(
					new ResourceStreamRequestHandler(
							new AbstractStringResourceStream() {
								private static final long serialVersionUID = 1L;
								@Override
								protected String getString() {
									return getModel().getObject().getText();
								}
							}));
		}
	}

	
	public class DrawingFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		
		public DrawingFragment(String id, IModel<? extends Response> model) {
			super(id, "drawingFragment", ResponseViewer.this, model);
			add(new SvgImage("drawing", model, maxWidth, maxHeight));
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
			download.setVisible(displayDownloadLink);
			download.add(new Label("filename", mResponseName));
			this.replace(download);

			Label fielname = new Label("filename", mResponseName);
			fielname.setVisible(displayTitle);
			this.replace(fielname);

			
			//Displayed Image			
			if (getModel().getObject().getResponseData().getBinaryFileData().getPrimaryType().equals("image")) {
				Image displayImage;
				PageParameters pp = new PageParameters()
					.add("id", mResponse.getObject().getResponseData().getBinaryFileData().getId());
				if (maxWidth==null && maxHeight==null) {
					// no scaling
					displayImage = new Image("imageDisplay", new UploadedFileResourceReference(), pp);
				} else {
					// Thumbnail class just takes a single dimension, so
					// scale to minimum of maxWidth or maxHeight
					Integer maxSize = maxWidth;
					if (maxHeight != null && (maxSize==null || maxHeight < maxSize))
						maxSize = maxHeight;
					displayImage = new Image("imageDisplay", new ThumbnailUploadedImageResourceReference(maxSize), pp);
				}
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
			
			//during runtime this is replaced in onBeforeRender			
			add(new EmptyPanel("filename"));
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
	
