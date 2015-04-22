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
package net.databinder.auth.data.hib;

import java.io.Serializable;
import java.security.MessageDigest;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import net.databinder.auth.AuthApplication;
import net.databinder.auth.data.DataPassword;

import org.apache.wicket.Application;
import org.apache.wicket.util.crypt.Base64;

/**
 * Simple, optional implementation of {@link DataPassword}. Maps as an embedded
 * property to the single field "passwordHash".
 * @author Nathan Hamblen
 */
@Embeddable
public class BasicPassword implements DataPassword, Serializable {
	private static final long serialVersionUID = 1L;
	private String passwordHash;
	
	public BasicPassword() { }
	
	public BasicPassword(String password) {
		change(password);
	}
	
	@Override
	public void change(String password) {
		MessageDigest md = ((AuthApplication)Application.get()).getDigest();
		byte[] hash = md.digest(password.getBytes());
		passwordHash = new String(Base64.encodeBase64(hash));
	}
	
	@Override
	public void update(MessageDigest md) {
		md.update(passwordHash.getBytes());
	}
	
	@Column(length = 28, nullable = false)
	private String getPasswordHash() {
		return passwordHash;
	}

	@SuppressWarnings("unused")
	private void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	@Override
	public boolean matches(String password) {
		return passwordHash != null &&
			passwordHash.equals(new BasicPassword(password).getPasswordHash());
	}
}
