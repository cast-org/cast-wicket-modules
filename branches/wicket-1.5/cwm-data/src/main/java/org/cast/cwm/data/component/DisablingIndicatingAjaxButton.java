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

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.cast.cwm.JQueryHeaderContributor;

/**
 * An instance of {@link IndicatingAjaxButton} that can disable components during
 * the AJAX request.
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public abstract class DisablingIndicatingAjaxButton extends IndicatingAjaxButton implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	
	public DisablingIndicatingAjaxButton(String id) {
		super(id);
	}
	public DisablingIndicatingAjaxButton(String id, IModel<String> model) {
		super(id, model);
	}
	public DisablingIndicatingAjaxButton(String id, Form<?> form) {
		super(id, form);
	}	
	public DisablingIndicatingAjaxButton(String id, IModel<String> model, Form<?> form) {
		super(id, model, form);
	}

	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator() {
		return new DisablingAjaxCallDecorator(getComponents());
	}
	
	public void renderHead(final IHeaderResponse response) {
		new JQueryHeaderContributor().renderHead(response);
		response.renderJavaScriptReference(DisablingAjaxCallDecorator.getJSResourceReference());
	}
	
	/**
	 * Returns a list of components (including containers) that should be disabled
	 * during the Ajax request.
	 * 
	 * @return
	 */
	protected abstract Collection<? extends Component> getComponents();

}
