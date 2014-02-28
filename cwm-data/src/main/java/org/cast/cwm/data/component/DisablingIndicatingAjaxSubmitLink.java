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
package org.cast.cwm.data.component;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;

/**
 * An instance of {@link AjaxSubmitLink} that can disable components during
 * the AJAX request AND display an Indicator, since there is no such thing as 
 * IndicatingAjaxSubmitLink.
 * 
 * @author jbrookover
 *
 * @param <T>
 */
public abstract class DisablingIndicatingAjaxSubmitLink 
		extends AjaxSubmitLink 
		implements IAjaxIndicatorAware {

	private static final long serialVersionUID = 1L;
	private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

	public DisablingIndicatingAjaxSubmitLink(String id) {
		this(id, null);
	}

	public DisablingIndicatingAjaxSubmitLink(String id, Form<?> form) {
		super(id, form);
		setOutputMarkupId(true);
		add(indicatorAppender);
	}
	
	/**
	 * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
	 */
	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return indicatorAppender.getMarkupId();
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new DisablingAjaxCallListener());
	}

	/**
	 * Returns a list of components (including containers) that should be disabled
	 * during the Ajax request.
	 * 
	 * @return
	 */
	protected abstract Collection<? extends Component> getComponents();



}
