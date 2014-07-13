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
