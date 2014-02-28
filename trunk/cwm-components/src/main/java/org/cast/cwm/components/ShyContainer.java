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
package org.cast.cwm.components;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A WebMarkupContainer that makes itself invisible whenever all immediate
 * children are invisible.
 */
public class ShyContainer extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public ShyContainer(String id) {
		super(id);
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public boolean isVisible() {
		return someChildVisible();
	}

	private boolean someChildVisible() {
		Boolean found = this.visitChildren(new IVisitor<Component,Boolean>(){

			@Override
			public void component(Component component, final IVisit<Boolean> visit) {
				if (determineVisibility(component)) {
					visit.stop(true);
				} else {
					visit.dontGoDeeper();
				}
			}

			private boolean determineVisibility(Component component) {
				component.configure();
				return component.determineVisibility();
			}

		});
		return (found != null && found);
	}

}
