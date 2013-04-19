/*
 * Copyright 2011-2013 CAST, Inc.
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

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;

public interface IUserContentService {

	/**
	 * Get a single UserContent by a given user for a given prompt.  This assumes that only
	 * one such response should exist.  If multiple responses are returned, an exception
	 * will be thrown.  In such cases, use ...
	 * 
	 * @param mPrompt model of the Prompt
	 * @param mUser model of the User
	 * @return model of the single UserContent; won't be null but may return null from getObject.
	 */
	IModel<UserContent> getResponseForPrompt(IModel<? extends Prompt> mPrompt, IModel<User> mUser);

}
