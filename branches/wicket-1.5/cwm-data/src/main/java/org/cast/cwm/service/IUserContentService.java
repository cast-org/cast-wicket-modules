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
