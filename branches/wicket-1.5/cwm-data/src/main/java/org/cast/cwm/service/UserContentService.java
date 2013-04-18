package org.cast.cwm.service;

import net.databinder.models.hib.BasicCriteriaBuilder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;
import org.hibernate.criterion.Restrictions;

public class UserContentService implements IUserContentService {

	public IModel<UserContent> getResponseForPrompt(IModel<? extends Prompt> mPrompt, IModel<User> mUser) {
		return new HibernateObjectModel<UserContent>(UserContent.class, 
				new BasicCriteriaBuilder(
						Restrictions.eq("prompt", mPrompt.getObject()),
						Restrictions.eq("user", mUser.getObject())));
	}

	
	
}
