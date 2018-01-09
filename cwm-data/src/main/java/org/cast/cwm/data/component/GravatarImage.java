/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm.data.component;

import java.security.MessageDigest;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;
import org.cast.cwm.data.User;


/**
 * A class for converting e-mail strings into md5 hash strings
 * for use with Gravatar.
 * 
 * Source: http://en.gravatar.com/site/implement/java
 * 
 * @author jbrookover
 * 
 *
 */
public class GravatarImage extends WebComponent {
	
	private static final long serialVersionUID = 1L;
	private static final String GRAVATAR_URL = "http://www.gravatar.com/avatar/";
	private static final int DEFAULT_SIZE = 80;
	
	public static enum DefaultImage {
		
		DEFAULT(""), HTTP_404("404"), IDENTICON("identicon"), MONSTERID("monsterid"), WAVATAR("wavatar"), MYSTERY_MAN("mm"), RETRO("retro");
		
		private String param;
		
		private DefaultImage(String param) {
			this.param = param;
		}
		
		@Override
		public String toString() {
			return param;
		}
	}
	
	@Getter private int size = DEFAULT_SIZE;
	@Getter @Setter private DefaultImage defaultImage = DefaultImage.RETRO;
	
	public GravatarImage(String id) {
		super(id);
	}
	
	public GravatarImage(String id, IModel<User> userModel) {
		super(id, userModel);
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		
		IValueMap attributes = tag.getAttributes();
		attributes.put("width", size);
		attributes.put("height", size);
		attributes.put("alt", "Profile Image for " + getDefaultModelObjectAsString());
		attributes.put("src", getUrl());
		
		super.onComponentTag(tag);
	}
	
	public GravatarImage setSize(int pixels) {
		if (pixels >= 1 && pixels <= 512)
			this.size = pixels;
		else
			throw new IllegalArgumentException("Gravatar image size must be between 1 and 512 pixels");
		return this;
	}
	
	/**
	 * Returns the appropriate URL for this image.  If the user (or e-mail) is null, defaults
	 * to the Gravatar Logo.
	 * 
	 * @return
	 */
	private String getUrl() {
		if (getDefaultModelObject() != null && ((User) getDefaultModelObject()).getEmail() != null) {
			String url = GRAVATAR_URL + md5Hex(((User)getDefaultModelObject()).getEmail().toLowerCase()) + formatUrlParameters();
			return url;
		} else
			return GRAVATAR_URL + formatUrlParameters();
	}

	private String formatUrlParameters() {
		 StringBuffer params = new StringBuffer();

		 if (size != DEFAULT_SIZE)
			 params.append("s=" + size);
		 
		 if (defaultImage != DefaultImage.DEFAULT)
			 params.append((params.length() == 0 ? "" : "&amp;") + "d=" + defaultImage);

		 if (params.length() == 0)
			 return "";
		 else
			 return "?" + params;
		 }
	
	    
	private static String md5Hex (String email) {         
		try {             
			MessageDigest md = 
				MessageDigest.getInstance("MD5");             
			return hex (md.digest(email.getBytes("UTF-8")));         
		} catch (Exception ex) {
			/* No Action */
		}
		return null;    
	}
	
	private static String hex(byte[] array) {        
		StringBuffer sb = new StringBuffer();        
		for (int i = 0; i < array.length; ++i) {            
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));        
		}        
		return sb.toString();    
	}
}

