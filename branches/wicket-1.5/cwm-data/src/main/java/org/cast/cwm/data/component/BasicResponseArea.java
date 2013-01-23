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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IResponseTypeRegistry;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.models.ResponseModel;
import org.cast.cwm.service.IResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.contrib.tinymce.settings.AutoResizePlugin;
import wicket.contrib.tinymce.settings.Button;
import wicket.contrib.tinymce.settings.TinyMCESettings;
import wicket.contrib.tinymce.settings.TinyMCESettings.Align;
import wicket.contrib.tinymce.settings.TinyMCESettings.Location;
import wicket.contrib.tinymce.settings.TinyMCESettings.Theme;
import wicket.contrib.tinymce.settings.TinyMCESettings.Toolbar;

import com.google.inject.Inject;

/**
 * A basic panel that provides a location to create responses to a prompt and
 * a list of those responses, each of which can be edited.
 * 
 * @author jbrookover
 *
 */
@AuthorizeInstantiation("STUDENT")
public class BasicResponseArea extends Panel implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BasicResponseArea.class);
	
	private static final String NEW_RESPONSE_ID = "newResponse";
	private static final String EXISTING_RESPONSE_ID = "existingResponse";
	private static final Integer RESPONSE_MAX_WIDTH = 550;
	
	private static TinyMCESettings editorSettings = null;
	
	private WebMarkupContainer responseListContainer;
	private WebMarkupContainer controlPanel;
	
	/**
	 * Page name, for logging.
	 */
	@Getter @Setter
	protected String pageName = null;
	
	/**
	 * Whether each response should have a header that includes the author, date, and user icon
	 */
	@Getter @Setter
	private boolean headerVisible = false;
	
	/**
	 * The prompt that this ResponseArea is responding to..
	 */
	private IModel<? extends Prompt> prompt;
	
	/**
	 * If true, responses from ALL users will be displayed for this prompt.  Otherwise,
	 * only the current users will be displayed;
	 * 
	 * TODO: ResponseAreas should take a user argument so teachers can see student responses.
	 */
	private boolean showAll;
	
	/**
	 * If a ResponseType is listed in this array, a link to create a response
	 * of that type will not be displayed.
	 */
	private Set<IResponseType> disabled = new HashSet<IResponseType>();
	
	private List<String> sentenceStarters = new ArrayList<String>();
	
	private List<String> stampURLs = new ArrayList<String>();
	
	/**
	 * Indicator for whether the user is currently editing a response.
	 */
	private boolean isEditing = false;

	@Inject
	protected IResponseService responseService;

	@Inject
	protected IResponseTypeRegistry typeRegistry;

	public BasicResponseArea(String id, IModel<? extends Prompt> model) {
		this(id, model, false);
	}
	
	public BasicResponseArea(String id, IModel<? extends Prompt> model, boolean showAll) {
		super(id, model);
		setOutputMarkupId(true);
		prompt = model;
		this.showAll = showAll;
		configureTinyMCE();
		addContent();
	}	
	
	private void configureTinyMCE() {
		// Configure TinyMCE in editor
		if (editorSettings == null) {
			editorSettings = new TinyMCESettings(Theme.advanced);
			editorSettings.setToolbarLocation(Location.top);
			editorSettings.setToolbarAlign(Align.left);
			editorSettings.setToolbarButtons(Toolbar.first, Arrays.asList(Button.bold, Button.italic, Button.strikethrough, Button.separator,
					Button.bullist, Button.numlist, Button.separator,
					Button.justifyleft, Button.justifycenter, Button.separator,
					Button.link, Button.unlink, Button.separator,
					Button.undo, Button.redo));
			List<Button> noButtons = Collections.emptyList();
			editorSettings.setToolbarButtons(Toolbar.second, noButtons);
			editorSettings.setToolbarButtons(Toolbar.third, noButtons);
			editorSettings.register(new AutoResizePlugin());
			ResponseEditor.setDefaultTinyMCESettings(editorSettings);
		}
	}
	
	private void addContent() {
		
		// Container for New Response Links
		controlPanel = new WebMarkupContainer("controls") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isEditing;
			}
		};
		controlPanel.setOutputMarkupPlaceholderTag(true);
		add(controlPanel);
		
		// Links for creating a new response
		controlPanel.add(new NewResponseLink("xmlText", typeRegistry.getResponseType("HTML")));
		controlPanel.add(new NewResponseLink("audio", typeRegistry.getResponseType("AUDIO")));
		controlPanel.add(new NewResponseLink("upload", typeRegistry.getResponseType("UPLOAD")));
		controlPanel.add(new NewResponseLink("draw", typeRegistry.getResponseType("SVG")));
				
		// Placeholder for a new response editor
		add(new WebMarkupContainer(NEW_RESPONSE_ID).setOutputMarkupPlaceholderTag(true).setVisible(false));
		
		// List of existing responses for this prompt
		responseListContainer = new WebMarkupContainer("responseListContainer");
		responseListContainer.setOutputMarkupId(true);
		add(responseListContainer);
		
		// TODO: Seems like this would be better with a DataView repeater.
		// TODO: Possible without hibernate references here?
		responseListContainer.add(new RefreshingView<Response>("responseList") {

			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<IModel<Response>> getItemModels() {
				
				Iterator<Response> responses = responseService.getResponsesForPrompt(prompt, showAll ? null : CwmSession.get().getUserModel()).getObject().iterator();

				return new ModelIteratorAdapter<Response>(responses) {

					@Override
					protected IModel<Response> model(Response object) {
						return new ResponseModel(object);
					}
				};
			}

			@Override
			protected void populateItem(Item<Response> item) {

				// Actual Response
				ResponseViewer rv = new ResponseViewer(EXISTING_RESPONSE_ID, item.getModel(), RESPONSE_MAX_WIDTH, null);
				item.add(rv);

				//headerVisible;
				//item.add(DateLabel.forDatePattern("timestamp", new PropertyModel<Date>(item.getModel(), "lastUpdated"), "M/d/yyyy - h:mma"));				
				IModel<Date> mDate = new PropertyModel<Date>(item.getModel(), "lastUpdated");
				item.add(new DateLabel("timestamp",mDate, new PatternDateConverter("yyyy-MM-dd HH:mm", false)){
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onConfigure() {
						setVisible(headerVisible);
						super.onConfigure();
					}					
				});
				item.add(new Label("author", new PropertyModel<String>(item.getModel(), "user")) {
					private static final long serialVersionUID = 1L;
					
					@Override
					public void onConfigure() {
						setVisible(headerVisible);
						super.onConfigure();
					}
				});					
					

				item.add(rv);

				item.add(new EditResponseLink("edit", item.getModel()).setVisible(item.getModelObject().getUser().equals(CwmSession.get().getUser())));
				item.setOutputMarkupId(true);
			}
		});
	}
	
	/**
	 * A link that opens an editor of the designated type. This can be used for creating 
	 * a new response for this prompt.
	 * 
	 * @author jbrookover
	 *
	 */
	protected class NewResponseLink extends AjaxFallbackLink<Void> {

		private static final long serialVersionUID = 1L;
		private IResponseType type;

		/**
		 * Represents a link that will open an editor of the given type.
		 * 
		 * @param id
		 * @param type
		 */
		public NewResponseLink(String id, IResponseType type) {
			super(id);
			this.type = type;
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
		
			isEditing = true;
			
			ResponseEditor editor = new ResponseEditor(NEW_RESPONSE_ID, prompt, type) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
					hideEditor(target);
				}
				
				@Override
				protected void onCancel(AjaxRequestTarget target) {
					hideEditor(target);
				}
				
				@Override
				protected void onDelete(AjaxRequestTarget target) {
					hideEditor(target);
				}
			};
			editor.setPageName(pageName);
			
			if (type.getName().equals("HTML")) {
				if (!sentenceStarters.isEmpty())
					editor.setStarters(sentenceStarters);
			} else if (type.getName().equals("SVG")) {
				if (!stampURLs.isEmpty())
					editor.setStarters(stampURLs);
			}
			
			BasicResponseArea.this.replace(editor);
			
			if (target != null) {
				addEditLinksToTarget(target);
				target.addComponent(controlPanel);
				target.addComponent(BasicResponseArea.this.get(NEW_RESPONSE_ID));
			}	
		}
		
		@Override
		public boolean isVisible() {
			return !disabled.contains(type);
		}

		private void hideEditor(AjaxRequestTarget target) {
			isEditing = false;
			
			BasicResponseArea.this.replace(new WebMarkupContainer(NEW_RESPONSE_ID).setOutputMarkupPlaceholderTag(true).setVisible(false));
			if (target != null) {
				target.addComponent(responseListContainer);
				target.addComponent(controlPanel);
				target.addComponent(BasicResponseArea.this.get(NEW_RESPONSE_ID));
			}
		}
	}
	
	protected class EditResponseLink extends AjaxFallbackLink<Response> {

		private static final long serialVersionUID = 1L;

		/**
		 * Represents a link that will open an editor of the given type.
		 * 
		 * @param id
		 * @param type
		 */
		public EditResponseLink(String id, IModel<Response> response) {
			super(id, response);
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			isEditing = true;

			ResponseEditor editor = new ResponseEditor(EXISTING_RESPONSE_ID, getModel()) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSave(AjaxRequestTarget target) {
					hideEditor(target);
				}
				
				@Override
				protected void onCancel(AjaxRequestTarget target) {
					hideEditor(target);
				}
				
				@Override
				protected void onDelete(AjaxRequestTarget target) {
					hideEditor(target);
				}
			};
			editor.setPageName(pageName);
			
			String type = getModelObject().getType().getName(); 
			if (type.equals("HTML")) {
				if (!sentenceStarters.isEmpty())
					editor.setStarters(sentenceStarters);
			} else if (type.equals("SVG")) {
				if (!stampURLs.isEmpty())
					editor.setStarters(stampURLs);
			}
			
			getParent().replace(editor);
			
			if (target != null) {
				addEditLinksToTarget(target);
				target.addComponent(controlPanel);
				target.addComponent(getParent().get(EXISTING_RESPONSE_ID));
			}	
		}

		private void hideEditor(AjaxRequestTarget target) {

			isEditing = false;
			getParent().replace(new ResponseViewer(EXISTING_RESPONSE_ID, getModel(), RESPONSE_MAX_WIDTH, null));
			if (target != null) {
				target.addComponent(responseListContainer);
				target.addComponent(controlPanel);
			}
		}
		
		@Override
		public boolean isVisible() {
			return !isEditing && super.isVisible();
		}
	}
	
	/**
	 * Adds the Control Panel and all Edit Links to the target.
	 * 
	 * @param target
	 */
	private void addEditLinksToTarget(final AjaxRequestTarget target) {
		
		responseListContainer.visitChildren(EditResponseLink.class, new IVisitor<EditResponseLink>() {

			public Object component(EditResponseLink component) {
				target.addComponent(component);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
			
		});
	}
	
	public BasicResponseArea setDisabled(IResponseType... types) {
		for(IResponseType type : types) {
			disabled.add(type);
		}
		return this;
	}
	
	public BasicResponseArea setAllDisabled() {
		for(IResponseType type : typeRegistry.getLegalResponseTypes()) {
			disabled.add(type);
		}
		return this;
	}
	
	public void addSentenceStarter(String s) {
		sentenceStarters.add(s);
	}
	
	public void addStampURL(String s) {
		stampURLs.add(s);
	}
	
	public void renderHead(IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference(BasicResponseArea.class, "buttons.css"));
	} 
	
	@Override
	protected void onDetach() {
		prompt.detach();
		super.onDetach();
	}
}
