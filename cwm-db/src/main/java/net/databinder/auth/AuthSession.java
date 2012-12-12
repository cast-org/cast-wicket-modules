/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.auth;

import net.databinder.auth.data.DataUser;

import org.apache.wicket.model.IModel;

/** Required interface for user session of applications using Databinder authentication. */
public interface AuthSession<T extends DataUser> {
	/** Sign in without setting cookie. */
	public boolean signIn(String username, String password);
	public boolean signIn(String username, String password, boolean setCookie);
	/** Sign in without checking password (here). */
	public void signIn(T user, boolean setCookie);
	public T getUser();
	public IModel<T> getUserModel();
	public boolean isSignedIn();
	/** Sign out and remove any authentication cookies. */
	public void signOut();
}
