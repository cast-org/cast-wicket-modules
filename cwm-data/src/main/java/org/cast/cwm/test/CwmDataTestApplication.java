/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.test;

import net.databinder.auth.AuthApplication;
import org.apache.wicket.markup.html.WebPage;
import org.cast.cwm.data.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Fake Application for testing that implements AuthApplication.
 * This is expected by certain Databinder methods, like those that work with passwords.
 *
 * @author bgoldowsky
 */
public class CwmDataTestApplication extends CwmTestApplication
	implements AuthApplication<User> {


	public CwmDataTestApplication(Map<Class<? extends Object>, Object> map) {
		super(map);
	}

	@Override
	public Class<User> getUserClass() {
		return User.class;
	}

	@Override
	public User getUser(String username) {
		return null;
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		return null;
	}

	@Override
	public byte[] getSalt() {
		return new byte[0];
	}

	@Override
	public MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getToken(User user) {
		return null;
	}
}
