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
package org.cast.cwm.service;

import java.util.List;

import net.databinder.models.hib.BasicCriteriaBuilder;
import net.databinder.models.hib.HibernateListModel;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class UserContentService implements IUserContentService {
	
	@Override
	public UserContent newUserContent (User author, IResponseType dataType) {
		return new UserContent(author, dataType);
	}


	@Override
	public IModel<UserContent> getUserContentForPrompt(IModel<? extends Prompt> mPrompt, IModel<User> mUser) {
		return new HibernateObjectModel<UserContent>(UserContent.class, 
				new BasicCriteriaBuilder(
						Restrictions.eq("prompt", mPrompt.getObject()),
						Restrictions.eq("user", mUser.getObject())));
	}


	@Override
	public IModel<List<UserContent>> getUserContentListForPrompt(
			IModel<? extends Prompt> mPrompt, IModel<User> mUser) {
		return new HibernateListModel<UserContent>(UserContent.class,
				new BasicCriteriaBuilder(
						Order.asc("sortOrder"),
						Restrictions.eq("prompt", mPrompt.getObject()),
						Restrictions.eq("user", mUser.getObject())));
	}

	
	
}
