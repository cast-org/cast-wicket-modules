package org.cast.cwm.data.models;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.AbstractPropertyModel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;

public class UserDisplayNameModel extends AbstractPropertyModel<String> {

	@Inject
	protected ICwmSessionService cwmSessionService;
	
	private static final long serialVersionUID = 1L;
	
	public UserDisplayNameModel (IModel<User> mUser) {
		super(mUser);
		Injector.get().inject(this);
	}

	@Override
	protected String propertyExpression() {
		User user = cwmSessionService.getUser();
		if (user == null)
			return "username";
		switch (user.getRole()) {
		case ADMIN:
		case RESEARCHER:
			return "subjectId";
		case TEACHER:
			return "fullName";
		case STUDENT:
		case GUEST:
			return "username";
		default:
			throw new IllegalArgumentException("Unknown role: " + user);
		}
	}

}
