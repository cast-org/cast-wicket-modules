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
package org.cast.cwm.data.component.highlight;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IResponseTypeRegistry;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.data.behavior.AjaxAutoSavingBehavior;
import org.cast.cwm.service.HighlightService.HighlightType;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IHighlightService;
import org.cast.cwm.service.IResponseService;
import org.cast.cwm.service.IUserPreferenceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A panel that outputs several hidden input fields for each registered highlighter.  These
 * forms are updated via javascript and saved automatically, if applicable.
 * 
 * NOTE: CSS for the field is not supplied by this component.
 * 
 * @author jbrookover
 *
 */
public class HighlightDisplayPanel extends Panel implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The stored highlights from the datastore.  Used to set the initial
	 * models of the hidden fields.
	 */
	@Getter(AccessLevel.PROTECTED)
	private Map<Character, String> highlights;
	
	@Getter @Setter
	private boolean readOnly = false; // Whether the highlighting should be 'readOnly'
	
	@Getter @Setter
	private boolean saveState = false; // Whether the highlighting should save the state
	
	private IModel<User> mUser; // Target user to display

	@Inject
	protected IResponseService responseService;
	
	@Inject
	protected IHighlightService highlightService;
	
	@Inject
	protected IUserPreferenceService preferenceService;

	@Inject
	protected ICwmSessionService cwmSessionService;

	public HighlightDisplayPanel(String id, IModel<Prompt> model) {
		this(id, model, null);
	}
	
	public HighlightDisplayPanel(String id, IModel<Prompt> model, IModel<User> targetUser) {
		super(id, model);
		mUser = (targetUser == null ? CwmSession.get().getUserModel() : targetUser);
		add(new HighlightDisplayForm("highlightDisplay", responseService.getResponseForPrompt(model, mUser)));
	}

	
	/**
	 * A form that contains the highlighting information for this page.  The information is stored in a set
	 * of hidden fields that initialized from the database and updated via javascript.  Submitting the form
	 * will save the highlights to the database.
	 * 
	 * @author jbrookover
	 *
	 */
	private class HighlightDisplayForm extends Form<Response> {
		
		private static final long serialVersionUID = 1L;
		
		@Inject
		protected IResponseTypeRegistry typeRegistry;

		@SuppressWarnings("unchecked")
		public HighlightDisplayForm(String id, IModel<Response> model) {
			super(id, model);
			
			// If there are no existing highlights, create new Response
			if (getModelObject() == null)
				setModel(responseService.newResponse(mUser, typeRegistry.getResponseType("HIGHLIGHT"), (IModel<Prompt>) HighlightDisplayPanel.this.getDefaultModel()));
			
			highlights = highlightService.decodeHighlights(model.getObject() == null ? "" : model.getObject().getResponseData().getText());
			
			// Add list of pre-defined highlighters
			add(new ListView<HighlightType>("colorDisplayList", highlightService.getHighlighters()) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<HighlightType> item) {
					item.setRenderBodyOnly(true);
					HiddenHighlightField field = new HiddenHighlightField("input", new PropertyModel<Character>(item.getModel(), "color"));
					item.add(field);
				}
			}.setRenderBodyOnly(true));
		}

		@Override
		protected void onSubmit() {
			
			final List<Character> colors = new ArrayList<Character>();
			final List<String> highlightsToSave = new ArrayList<String>();
			
			HighlightDisplayForm.this.visitChildren(HiddenHighlightField.class, new IVisitor<HiddenHighlightField,Void>() {

				public void component(HiddenHighlightField component, IVisit<Void> visit) {
					String s = component.getModelObject();
					if (s != null &&! s.isEmpty()) {
						colors.add(component.getColorCode().getObject());
						highlightsToSave.add(s);
					}
				}
			});
						
			String encodedString = highlightService.encodeHighlights(colors, highlightsToSave);

			// TODO: Need a page name...how?
			responseService.saveTextResponse(this.getModel(), encodedString, null);
		}
		
		@Override
		protected void onBeforeRender() {
			if (!readOnly)
				add(new AjaxAutoSavingBehavior(this));
			super.onBeforeRender();
		}
	}
	
	public void renderHead(IHeaderResponse response) {
		// FIXME: used to refer to, but not supply, a CSS file.  That's not really helpful.
		// Caller should supply any necessary CSS.
		// response.renderCSSReference(UrlUtils.rewriteToContextRelative("css/highlight.css", RequestCycle.get().getRequest()));
		response.renderJavaScriptReference(new PackageResourceReference(HighlightDisplayPanel.class, "rangy-core.js"));
		response.renderJavaScriptReference(new PackageResourceReference(HighlightDisplayPanel.class, "new-highlight.js"));
		
		response.renderOnDomReadyJavaScript(getHighlighterInitScript()); 
	}

	private String getHighlighterInitScript() {
		// determine initially-selected color, if any
		IModel<User> mUser = cwmSessionService.getUserModel();
		Boolean prefValue = preferenceService.getUserPreferenceBoolean(mUser, "highlightOn");
		boolean highlightOn = (prefValue==null) ? false : prefValue;

		String highlightColor = "";
		if (highlightOn) {
			highlightColor = preferenceService.getUserPreferenceString(mUser, "highlightColor");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("$().CAST_Highlighter({colors:[");
		sb.append(getColors());
		sb.append("], readonly: ");
		sb.append(readOnly);
		sb.append(", saveState: " + saveState);
		if (saveState && !Strings.isEmpty(highlightColor))
			sb.append(", initialColor: '" + highlightColor + "'");
		sb.append("});");
		return sb.toString();
	}

	private String getColors() {
		List<HighlightType> highlighters = highlightService.getHighlighters();
		List<String> colors = new ArrayList<String>();
		for (HighlightType h: highlighters)
			colors.add("'" + h.getColor() + "'");
		return StringUtils.join(colors, ",");
	}

	
	/**
	 * A hidden field that knows what color highlight it is displaying.
	 * 
	 * @author jbrookover
	 *
	 */
	public class HiddenHighlightField extends HiddenField<String> {

		private static final long serialVersionUID = 1L;

		@Getter private IModel<Character> colorCode;
		
		public HiddenHighlightField(String id, IModel<Character> color) {
			super(id, new Model<String>());
			this.colorCode = color;
			setOutputMarkupId(true);
		}
		
		@Override
		protected void onBeforeRender() {
			setModelObject(highlights.get(colorCode.getObject()));
			super.onBeforeRender();
		}
		
		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			checkComponentTag(tag, "input");
			tag.put("class", colorCode.getObject() + "highlightedWords");
		}
	}
}
