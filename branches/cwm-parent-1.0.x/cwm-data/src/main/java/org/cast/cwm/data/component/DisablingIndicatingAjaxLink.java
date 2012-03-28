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

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;

/**
 * An instance of {@link IndicatingAjaxLink} that can disable components during
 * the AJAX request.
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public abstract class DisablingIndicatingAjaxLink<T> extends IndicatingAjaxLink<T> implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	public DisablingIndicatingAjaxLink(String id) {
		this(id, null);
	}
	
	public DisablingIndicatingAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator() {
		return new DisablingAjaxCallDecorator(getComponents());
	}
	
	public void renderHead(final IHeaderResponse response) {
		response.renderJavascriptReference(DisablingAjaxCallDecorator.getJSResourceReference());
	}
	
	/**
	 * Returns a list of components (including containers) that should be disabled
	 * during the Ajax request.
	 * 
	 * @return
	 */
	protected abstract Collection<? extends Component> getComponents();

}
