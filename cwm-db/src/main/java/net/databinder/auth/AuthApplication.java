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

import java.security.MessageDigest;

import net.databinder.auth.data.DataUser;

import org.apache.wicket.markup.html.WebPage;

/**
 * Application-specific authorization settings. Many components of Databinder authentication
 * require that this be implemented by the current WebApplication instance.
 * @author Nathan Hamblen
 */
public interface AuthApplication<T extends DataUser> {
	/**
	 * @return class to be used for signed in users
	 */
	public Class<T> getUserClass();
	/** 
	 * @return DataUser for the given username. 
	 */
	public T getUser(String username);
	/**
	 * @return page to sign in users
	 */
	public Class< ? extends WebPage> getSignInPageClass();
	/**
	 * Cryptographic salt to be used in authentication. The default getDigest()
	 * implementation uses this value.
	 * @return app-specific salt
	 */
	public abstract byte[] getSalt();
	
	/** @return application-salted hashing digest */
	public MessageDigest getDigest();
	
	/**
	 * Get the restricted token for a user, passing an appropriate location parameter. 
	 * @param user source of token
	 * @return restricted token
	 */
	public String getToken(T user);
}
