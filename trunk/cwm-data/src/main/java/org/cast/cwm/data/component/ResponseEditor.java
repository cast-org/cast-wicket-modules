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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.audioapplet.component.AbstractAudioRecorder;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.behavior.AjaxAutoSavingBehavior;
import org.cast.cwm.data.behavior.ChromeFrameUtils;
import org.cast.cwm.data.behavior.MaxLengthAttribute;
import org.cast.cwm.data.models.LoadableDetachableAudioAppletModel;
import org.cast.cwm.drawtool.SvgEditor;
import org.cast.cwm.service.ResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.ajax.TinyMceAjaxSubmitModifier;
import wicket.contrib.tinymce.settings.TinyMCESettings;

/**
 * Base class for editing any type of response.  This class handles the actual saving
 * of a response.  Override {@link #onSave(AjaxRequestTarget)}, {@link #onCancel(AjaxRequestTarget)},
 * and {@link #onDelete(AjaxRequestTarget)} to provide additional behavior, such as redrawing
 * a display.
 * 
 * TODO: Does this need to be abstract?
 * 
 * TODO: Add response limit options (upload, text length, recording length, etc).
 * 
 * @author jbrookover
 *
 */
public abstract class ResponseEditor extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ResponseEditor.class);
	
	@Getter @Setter protected IModel<Prompt> prompt;
	@Getter @Setter protected String pageName;
	@Getter @Setter protected IResponseType type;
	@Getter @Setter protected List<String> starters = new ArrayList<String>();
	@Getter @Setter protected String templateURL;
	@Getter @Setter protected FeedbackPanel feedbackPanel;
	@Getter @Setter protected boolean cancelVisible = true;
	@Getter @Setter protected boolean saveVisible = true;
	@Getter @Setter protected boolean deleteVisible = true;
	@Getter @Setter protected boolean autoSave = true; // Only applies to Text/Drawing
	@Getter @Setter protected boolean debug = false; // Turn on editor debugging? Only applies to Audio.

	// TODO: Extend for upload, recording, etc?  Overload?
	// TODO: Untested with TinyMCE
	@Getter @Setter protected Integer maxlength; // Maximum Text Length (characters)
	
	private WebMarkupContainer cancelButton;
	private WebMarkupContainer deleteButton;

	/**
	 * Default settings object that will be used by ResponseEditor.
	 * You can arrange for different behavior by overriding {@link #getTinyMCESettings()}
	 */
	private static TinyMCESettings defaultTinyMCESettings;

	// Used to determine the original response for cancel and delete purposes
	@Getter @Setter protected IModel<Response> mOriginalResponse;
	@Getter @Setter protected boolean newResponse = false;
	@Getter @Setter protected boolean hasAutoSaved = false;

	/**
	 * Creates a panel with no existing data, attached to the given prompt.
	 * Used for creating a new response.
	 * 
	 * @param id
	 * @param prompt
	 */
	public ResponseEditor(String id, IModel<? extends Prompt> prompt, IResponseType type) {
		this(id, ResponseService.get().newResponse(CwmSession.get().getUserModel(), type, prompt));
		newResponse = true;
	}
	
	/**
	 * Creates a panel using the existing ResponseData model.  
	 * Used for editing an existing response.
	 * 
	 * @param id
	 * @param model
	 */
	public ResponseEditor(String id, IModel<Response> model) {
		super(id, model);
		this.prompt = new PropertyModel<Prompt>(model, "prompt");
		this.type = model.getObject().getType();
		mOriginalResponse = model;
		setOutputMarkupPlaceholderTag(true);
	}
		
	@Override
	protected void onInitialize() {
		super.onInitialize();
		addEditor();
	}

	/**
	 * Adds the editor for this Response.  Also adds 'cancel' and 'delete'
	 * buttons for this Response.
	 * 
	 * NOTE: Each editor needs to call {@link #onSave(AjaxRequestTarget)} to save
	 * the response itself.
	 * 
	 * @see Response
	 * 
	 */
	private void addEditor() {
		WebMarkupContainer editor = getEditorFragment("editor", getModel(), type);
		add(editor);

		// Cancel Button/Dialog
		// TODO: Make this 'indicating', probably doesn't need to block because it already does.
		AjaxCancelChangesDialog cancelDialog = new AjaxCancelChangesDialog("cancelModal") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				if (newResponse) {
					// cancel a new response - remove that response
					ResponseService.get().deleteResponse(getModel());
					setModelObject(null);
				} else if (hasAutoSaved){
					log.debug("CANCEL EDITS ON EXISTING RESPONSE");
					// FIXME:  Need to implement a transaction rollback here - ldm
					// if you have already autosaved and then the user cancels - restore the original response
					hasAutoSaved = false;
				}
				target.addComponent(feedbackPanel);
				ResponseEditor.this.onCancel(target);
			}
		};
		add(cancelDialog);
		cancelButton = new WebMarkupContainer("cancel")  {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return cancelVisible;
			}
		};
		cancelButton.add(cancelDialog.getClickToOpenBehavior()).setOutputMarkupId(true);
		editor.add(cancelButton);
		
		// Delete Button/Dialog
		// TODO: Make this 'indicating', probably doesn't need to block because it already does.
		AjaxDeletePersistedObjectDialog<Response> deleteDialog = new AjaxDeletePersistedObjectDialog<Response>("deleteModal", getModel()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void deleteObject(AjaxRequestTarget target) {
				ResponseService.get().deleteResponse(getModel());
				setModelObject(null);
				target.addComponent(feedbackPanel);
				onDelete(target);				
			}			
		};		
		deleteDialog.setObjectName("response");
		add(deleteDialog);
		deleteButton = new WebMarkupContainer("delete") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !getModelObject().isTransient() && deleteVisible;
			}
		};
		deleteButton.add(deleteDialog.getDialogBorder().getClickToOpenBehavior()).setOutputMarkupId(true);
		editor.add(deleteButton);
		
		
		// Feedback Panel
		feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setFilter(new ContainerFeedbackMessageFilter(ResponseEditor.this));
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);
	}
	
	/**
	 * Return the appropriate editor fragment for the 
	 * @param wicketId
	 * @param model
	 * @param type
	 */
	protected WebMarkupContainer getEditorFragment(String id, IModel<Response> model, IResponseType type) {
		String typeName = type.getName();
		if (typeName.equals("TEXT"))
			return (new TextFragment(id, model, false));
		if (typeName.equals("HTML"))
			return (new TextFragment(id, model, true));
		if (typeName.equals("AUDIO"))
			return (new AudioFragment(id, model));
		if (typeName.equals("UPLOAD"))
			return (new UploadFragment(id, model));
		if (typeName.equals("SVG"))
			return (new DrawingFragment(id, model));
		return null;
	}

	/**
	 * Editor for either Plain Text (using a textarea) or WYSIWYG Text (using TinyMCE).
	 * 
	 * @author jbrookover
	 *
	 */
	protected class TextFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		private DropDownChoice<String> choiceList;

		public TextFragment(String id, IModel<Response> model) {
			this(id, model, false);
		}
		
		public TextFragment(String id, IModel<Response> model, final boolean useWysiwyg) {
			super(id, "textFragment", ResponseEditor.this, model);
			
			/*
			 * Form for Text Box
			 */
			Form<Response> form = new Form<Response>("form", model) {

				private static final long serialVersionUID = 1L;
				
				@Override
				public void onSubmit() {
					super.onSubmit();
					String message = this.get("message").getDefaultModelObjectAsString();
					ResponseService.get().saveTextResponse(getModel(), message, pageName);
				}
			};
			form.setOutputMarkupId(true);
			add(form);

			if (autoSave) {
				form.add(new AjaxAutoSavingBehavior(form) {

					private static final long serialVersionUID = 1L;

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						// Ensure TinyMCE saves to form before we check to see if the form changed.
						if (useWysiwyg)
							response.renderJavascript("AutoSaver.addOnBeforeSaveCallBack(function() {tinyMCE.triggerSave();});", "tinyMCEAutoSave");					
					}

					@Override
					protected void onAutoSave(AjaxRequestTarget target) {
						super.onAutoSave(target);
						hasAutoSaved = true;
						ResponseEditor.this.onAutoSave(target);
					}
				});
			}
			
			DisablingIndicatingAjaxSubmitLink save = new DisablingIndicatingAjaxSubmitLink("save", form) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
					onSave(target);
					// set new response flag and reset the new baseline response
					newResponse = false;
					mOriginalResponse = (IModel<Response>) getModel();
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
				}

				@Override
				protected Collection<? extends Component> getComponents() {
					return Arrays.asList(this, cancelButton, deleteButton);
				}

				@Override
				public boolean isVisible() {
					return saveVisible;
				}				
			};
			add(save);
			
			IModel<String> textModel = new Model<String>(((Response) getDefaultModelObject()).getText());
			TextArea<String> textArea = new TextArea<String>("message", textModel);
			if (useWysiwyg) {
				textArea.setEscapeModelStrings(false);
				textArea.add(new TinyMceBehavior(getTinyMCESettings()));
				save.add(new TinyMceAjaxSubmitModifier());
			}

			// TODO: Untested with TINYMCE!
			if (maxlength != null) {
				textArea.add(StringValidator.maximumLength(maxlength));
				textArea.add(new MaxLengthAttribute(maxlength));
			}

			textArea.setOutputMarkupId(true);
			form.add(textArea);
			
			/*
			 * Sentence Starter Dropdown List
			 * @see onBeforeRender for choice modifications and visibility.
			 */
			// TODO there needs to be a label in here attached to this pulldown
			add(choiceList = new DropDownChoice<String>("sentenceStarters", new Model<String>(null), new PropertyModel<List<String>>(ResponseEditor.this, "starters"), new SentenceStarterRenderer()));
			choiceList.setOutputMarkupId(true);
			choiceList.setNullValid(true);
			add(new WebMarkupContainer("addLink").add(new SimpleAttributeModifier("onclick", "tinyMCE.get('" + textArea.getMarkupId() + "').setContent(tinyMCE.get('" + textArea.getMarkupId() + "').getContent() + $('#" + choiceList.getMarkupId(true) + "').val()); return false;")));
			
		}

		@Override
		public void onBeforeRender() {
			// If there are no starters, make the dropdown/link invisible
			if (starters == null || starters.isEmpty()) {
				choiceList.setVisible(false);
				get("addLink").setVisible(false);
			}
			super.onBeforeRender();
		}

		private class SentenceStarterRenderer implements IChoiceRenderer<String> {
			private static final long serialVersionUID = 1L;

			public String getDisplayValue(String object) {
				return object;
			}

			public String getIdValue(String object, int index) {
				return object;
			}
		}
	}

	
	protected class AudioFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public AudioFragment(String id, IModel<Response> model) {
			super(id, "audioFragment", ResponseEditor.this, model);

			LoadableDetachableAudioAppletModel audioModel = new LoadableDetachableAudioAppletModel(model);
			audioModel.setMaxLength(300);
			
			AbstractAudioRecorder audioApplet = new AbstractAudioRecorder("applet", audioModel) {

				private static final long serialVersionUID = 1L;

				@Override
				public void onReceiveAudio() {
					String filename = ResponseEditor.this.getModel().getObject().getUser().getId() + "_" + Time.now().getMilliseconds() + ".au";
					ResponseService.get().saveBinaryResponse(ResponseEditor.this.getModel(), getModelObject(), "audio/au", filename, pageName);
				}

				@Override
				public void onSave(AjaxRequestTarget target) {
					ResponseEditor.this.onSave(target);
				}
				
			};
			audioApplet.setDebug(debug);
			add(audioApplet);

			WebMarkupContainer saveLink = audioApplet.generateStandardSaveLink("save");
			add(saveLink);
		}
	}
	
	protected class UploadFragment extends Fragment {

		private static final long serialVersionUID = 1L;
		private Form<Response> form;
		
		public UploadFragment(String id, IModel<Response> model) {
			super(id, "uploadFragment", ResponseEditor.this, model);
			
			form = new Form<Response>("form", model) {

				private static final long serialVersionUID = 1L;
				
				@Override
				public void onSubmit() {
					super.onSubmit();
					
					FileUpload fileUpload = ((FileUploadField) this.get("fileUploadField")).getFileUpload();
					if (fileUpload != null) {
						ResponseService.get().saveBinaryResponse(getModel(), fileUpload.getBytes(), fileUpload.getContentType(), fileUpload.getClientFileName(), pageName);
					}
				}
			};
			form.setOutputMarkupId(true);
			form.setMultiPart(true);
			add(form);
						
			form.add(new FileUploadField("fileUploadField", new Model<FileUpload>(null)).setRequired(true));
			form.setMaxSize(Bytes.megabytes(2));
			
			DisablingIndicatingAjaxSubmitLink save = new DisablingIndicatingAjaxSubmitLink("save", form) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
					onSave(target);			
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
				}

				@Override
				protected Collection<? extends Component> getComponents() {
					return Arrays.asList(this, cancelButton, deleteButton);
				}
			};
			add(save);
		}
	}
	
	
	// TODO: Drawing Autosave triggers an extra save when first editing a drawing because the format of the SVG is
	// different at random times (specifically attributes of the SVG come in a different order).  This causes
	// the same 'drawing' to be seen differently by the autosave script.
	protected class DrawingFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public DrawingFragment(String id, IModel<Response> model) {
			super(id, "drawingFragment", ResponseEditor.this, model);
			
			WebMarkupContainer svgNotSupported = new WebMarkupContainer("svgNotSupported");
			svgNotSupported.setVisible(false);
			add(svgNotSupported);
			
			String baseSvg = model.getObject().getText();
			
			// Create 'empty' SVG
			if (baseSvg == null) {
				
				baseSvg = "<svg width=\"" + SvgEditor.CANVAS_WIDTH + "\" height=\"" + SvgEditor.CANVAS_HEIGHT + "\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
				if (templateURL != null) {
					String url;
					try {
						url = new URI(RequestUtils.toAbsolutePath(templateURL)).getPath();
					} catch (URISyntaxException e) {
						throw new IllegalArgumentException(e);
					}

					baseSvg = baseSvg.concat("<g display=\"inline\"><title>Template</title>");
					baseSvg = baseSvg.concat("<image x=\"0\" y=\"0\" width=\"" + SvgEditor.CANVAS_WIDTH + "\" height=\"" + SvgEditor.CANVAS_HEIGHT + "\" id=\"svg_1\" xlink:href=\"" + url + "\" />");
					baseSvg = baseSvg.concat("</g>");
				}
				
				baseSvg = baseSvg.concat("<g display=\"inline\"><title>Layer 1</title></g>");
				baseSvg = baseSvg.concat("</svg>");
			}
			
			SvgEditor svgEditor = new SvgEditor();
			svgEditor.addExtension(new SvgUploadExtensionImpl(getModel()));
			
			svgEditor.setSvg(baseSvg);
			// Transform drawing starters into absolute URLs so that they work both in editor and viewer,
			// which have different base hrefs.
			if (starters != null) {
				ArrayList<String> starterUrls = new ArrayList<String>(starters.size());
				for (String starter : starters) {
					try {
						// TODO: Better way to determine external image
						starterUrls.add(starter.startsWith("http://") ? starter : new URI(RequestUtils.toAbsolutePath(starter)).getPath());
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
				svgEditor.setDrawingStarters(starterUrls);
			}
			
			final InlineFrame frame = svgEditor.getEditor("svgeditor");
			add(frame);

			Form<Response> form = new Form<Response>("form", model) {

				private static final long serialVersionUID = 1L;
				
				@Override
				public void onSubmit() {
					super.onSubmit();
					String svg = this.get("svg").getDefaultModelObjectAsString();
					svg = svg.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("[\\r\\n\\f]", " ");
					ResponseService.get().saveSVGResponse(getModel(), svg, pageName);
				}
			};
			final HiddenField<String> svg = new HiddenField<String>("svg", new Model<String>(""));
			svg.setOutputMarkupId(true);
			form.add(svg);
			form.setOutputMarkupId(true);
			add(form);
			
			if (autoSave) {
				form.add(new AjaxAutoSavingBehavior(form) {

					private static final long serialVersionUID = 1L;

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = 
							"AutoSaver.addOnBeforeSaveCallBack(function() { " +
							"   $('#" + svg.getMarkupId() + "').val(unescape($('#" + frame.getMarkupId() + "').get(0).contentDocument.svgCanvas.getSvgString()));" +
							"});";
						response.renderJavascript(script, "SvgEditorAutosave-" + svg.getMarkupId());					
					}

					@Override
					protected void onAutoSave(AjaxRequestTarget target) {
						super.onAutoSave(target);
						hasAutoSaved = true;
						ResponseEditor.this.onAutoSave(target);
					}

				});
			}
			
			DisablingIndicatingAjaxSubmitLink save = new DisablingIndicatingAjaxSubmitLink("save", form) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
					onSave(target);		
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.addComponent(feedbackPanel);
				}
				
	
				@Override
				protected IAjaxCallDecorator getAjaxCallDecorator() {
					return new DisablingAjaxCallDecorator(getComponents()) {
						
						private static final long serialVersionUID = 1L;

						@Override
						public CharSequence decorateScript(CharSequence script) {
							return "$('#" + svg.getMarkupId() + "').val(unescape($('#" + frame.getMarkupId() + "').get(0).contentDocument.svgCanvas.getSvgString())); " + super.decorateScript(script);
						}
					};
				}

				@Override
				protected Collection<? extends Component> getComponents() {
					return Arrays.asList(this, cancelButton, deleteButton);
				}

				@Override
				public boolean isVisible() {
					return saveVisible;
				}				
			};
			add(save);
		}
		
		@Override
		protected void onBeforeRender() {
			if (ChromeFrameUtils.isBareIE())
				replaceWith(new SvgNotSupportedFragment(this.getId()));
			super.onBeforeRender();
		}
	}
	
	protected class SvgNotSupportedFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public SvgNotSupportedFragment(String id) {
			super(id, "svgNotSupportedFragment", ResponseEditor.this);
			
			add(new AjaxFallbackLink<Void>("close") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					ResponseEditor.this.onCancel(target);
				}
				
			});
		}
	}
	
	/**
	 * Override this function to execute behavior when the Save button
	 * is clicked.  For example, this is where you can hide this response
	 * editor and update a list of responses.<br />
	 * <br />
	 * By default, this does nothing.
	 * 
	 * @param target
	 */
	protected void onSave(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Override this function to execute behavior when the Cancel button
	 * is clicked.  For example, this is where you can hide this response
	 * editor.<br />
	 * <br />
	 * By default, this does nothing.
	 * 
	 * @param target
	 */
	protected void onCancel(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Override this function to execute behavior when the Delete button
	 * is clicked.  For example, this is where you can hide this response
	 * editor.<br />
	 * <br />
	 * By default, this does nothing.
	 * 
	 * @param target
	 */
	protected void onDelete(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Override this function to execute behavior when the AutoSave behavior
	 * of this editor is triggered.  For example, this is where you can set
	 * a last updated field.<br />
	 * <br />
	 * By default, this does nothing.
	 * 
	 * @param target
	 */
	protected void onAutoSave(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<Response> getModel()
	{
		return (IModel<Response>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<Response> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	public final Response getModelObject()
	{
		return (Response)getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(Response object)
	{
		setDefaultModelObject(object);
	}

	/** Return a TinyMCE settings object that this editor will use for editing HTML Responses.
	 * By default, just uses deafultTinyMCESettings, but more interesting behavior may be arranged
	 * by overriding this method.
	 */
	public TinyMCESettings getTinyMCESettings() {
		return getDefaultTinyMCESettings();
	}
	
	/**
	 * Get static default TinyMCE settings object.
	 * @return
	 */
	public static TinyMCESettings getDefaultTinyMCESettings() {
		if (defaultTinyMCESettings == null)
			defaultTinyMCESettings = new TinyMCESettings();
		return defaultTinyMCESettings;
	}

	/**
	 * Set static default TinyMCE settings object.
	 * @param settings
	 */
	public static void setDefaultTinyMCESettings(TinyMCESettings settings) {
		defaultTinyMCESettings = settings;
	}

	@Override
	protected void onDetach() {
		prompt.detach();
		if (mOriginalResponse != null)
			mOriginalResponse.detach();
		super.onDetach();
	}
}
