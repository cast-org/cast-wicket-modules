/*
 * Copyright 2011-2016 CAST, Inc.
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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Role;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

/**
 * Container whose visibility is set based on the role of the logged-in user.
 * This container will be visible if the user has the {link #requiredRole}.
 * If the boolean higherRolesAllowed is set, then roles above the requiredRole are 
 * also allowed to see the component, otherwise checking is exact. 
 * 
 * @author bgoldowsky
 *
 */
public class RoleVisibilityContainer extends WebMarkupContainer {

	@Inject
	ICwmSessionService cwmSessionService;
	
	private static final long serialVersionUID = 1L;

	public RoleVisibilityContainer(String id, Role requiredRole, boolean higherRolesAllowed) {
		this(id, null, requiredRole, higherRolesAllowed);
	}

	public RoleVisibilityContainer(String id, IModel<?> model, Role requiredRole, boolean higherRolesAllowed) {
		super(id, model);
		setVisiblity(requiredRole, higherRolesAllowed);
	}

	private void setVisiblity(Role requiredRole, boolean higherRolesAllowed) {
		Role userRole = cwmSessionService.getUser().getRole();
		setVisible (higherRolesAllowed
						? userRole.subsumes(requiredRole)
						: userRole.equals(requiredRole));
	}

}
