/*
 * Copyright 2011-2015 CAST, Inc.
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

import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.cwm.IResponseTypeRegistry;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.IResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Basic Star Rating Panel  This is essentially both a {@link ResponseEditor} and
 * {@link ResponseViewer} for a {@link ResponseType.STAR_RATING}.
 * 
 * @author jbrookover
 */

@AuthorizeInstantiation(Role.STUDENT_ROLENAME)
public class StarPanel extends Panel implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StarPanel.class);

	private RadioGroup<Integer> radioGroup;

	/**
	 * Number of stars.
	 */
	private int numStars = 5;
	
	@Inject
	private IResponseService responseService;

	@Inject
	protected IResponseTypeRegistry typeRegistry;

	/**
	 * Constructor.  If the model object is null, this component will be invisible.
	 * 
	 * TODO: This hiding behavior is not ideal or expected.  Only used because this is
	 * both a viewer and editor.
	 * 
	 * @param id
	 * @param model
	 */
	public StarPanel(String id, IModel<Response> model) {
		super(id, model);

		setOutputMarkupId(true);
		add(new ClassAttributeModifier("starPanel"));

		if (model == null || model.getObject() == null) {
			setVisible(false);
			return;
		}
		
		
		if (!model.getObject().getType().equals(typeRegistry.getResponseType("STAR_RATING")))
			throw new IllegalArgumentException("A Star Rating panel must be attached to a ResponseType.STAR_RATING.");
		
		Form<Response> form = new Form<Response>("starForm", getModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				responseService.saveStarRating(getModel(), Integer.valueOf(radioGroup.getValue()).intValue());
			}
		}; 

		add(form);

		radioGroup = new RadioGroup<Integer>("radioGroup", new Model<Integer>()); // Model set in onBeforeRender()
		form.add(radioGroup);
		
		// TODO: This markup is not the best for accessibility.  The label should come after the radio, not wrap it.  However
		// changing would take a lot of work in the javascript.
		RepeatingView rv = new RepeatingView("radioRepeatingView");
		radioGroup.add(rv);
		for(int i = 1; i <= numStars; i++) {
			WebMarkupContainer item = new WebMarkupContainer(rv.newChildId());
			rv.add(item);
			
			Radio<Integer> radio = new Radio<Integer>("radio", new Model<Integer>(i));
			radio.setOutputMarkupId(true);
			
			Label label = new Label("label", i == 1 ? i + " Star" : i + " Stars");
			label.setRenderBodyOnly(true);
			item.add(new SimpleAttributeModifier("for", radio.getMarkupId()));
			item.add(radio);
			item.add(label);
		}
		
		form.add(new DisablingIndicatingAjaxButton("submitButton") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onSave(target);
				target.addComponent(StarPanel.this);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				onError(target, form);
			}

			@Override
			protected Collection<? extends Component> getComponents() {
				return Arrays.asList(StarPanel.this);
			}

		});
	}

	@Override
	protected void onBeforeRender() {
		
		// Must do this manually since we don't want the model to actually modify the ResponseData
		radioGroup.setModelObject(getModelObject().getScore());
		
		super.onBeforeRender();
	}
	
	public void renderHead(IHeaderResponse response) {
		
		response.renderJavaScriptReference(new PackageResourceReference(StarPanel.class, "star_rating.js"), "StarRating");
		response.renderCSSReference(getStarCSSReference());
		
		response.renderOnDomReadyJavaScript("starRating.create('#" + getMarkupId() + " .stars')");
	}
	
	/**
	 * Get the CSS for the star rating panel.  Override this method
	 * to provide different color stars/styles/etc.
	 * @return
	 */
	public PackageResourceReference getStarCSSReference() {
		return new PackageResourceReference(StarPanel.class, "star_rating.css");
	}
	
	/**
	 * Override this method to specify onSave behavior.
	 * @param score the rating that was selected
	 */
	public void onSave(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Override this method to specify onError behavior.
	 * 
	 * @param target
	 * @param form
	 */
	public void onError(AjaxRequestTarget target) {
		
	}
	
	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<Response> getModel()
	{
		return (IModel<Response>) getDefaultModel();
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
		return (Response) getDefaultModelObject();
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
}


