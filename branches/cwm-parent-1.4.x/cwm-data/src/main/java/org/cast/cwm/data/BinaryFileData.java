/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.data;

import java.util.Date;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.resource.UploadedFileResource;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

/**
 * An object that holds a binary file, such as an image
 * or an upload.
 * 
 * @author jbrookover
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of={"id", "name", "mimeType"})
public class BinaryFileData extends PersistedObject {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	private String name;
	
	private String mimeType;
	
	private byte[] data;
	
	private Date lastModified;
	
	protected BinaryFileData() {
		lastModified = new Date();
	}

	public BinaryFileData(String name, String mimeType, byte[] data) {
		this();
		this.name = name.replaceAll("\\s+", "_");
		this.mimeType = mimeType;
		this.data = data;
	}

	/**
	 * Attempts to parse the provided MimeType String and returns the primary type
	 * (eg image, audio, application, ....)
	 * @return the primary type, or null if it could not be determined.
	 */
	public String getPrimaryType() {
		MimeType type;
		try {
			type = new MimeType(mimeType);
		} catch (MimeTypeParseException ex) {
			ex.printStackTrace();
			return null;
		}
		return type.getPrimaryType();
	}
	
	/**
	 * Attempts to parse the provided MimeType String and returns whether
	 * this binary data represents an image.
	 * @return
	 */
	public boolean isImage() {
		return getPrimaryType().equals("image");
	}
	
	/**
	 * Deprecated Method.  Instead, use {@link UploadedFileResource}.
	 * 
	 * @return
	 */
	@Deprecated
	public ResourceReference getResourceReference() {
		String filename = id + "_" + name;

		if (Application.get().getSharedResources().get(filename) == null) {
			DynamicWebResource res = new DynamicWebResource(filename) {

				private static final long serialVersionUID = 1L;

				@Override
				protected ResourceState getResourceState() {
					return new ResourceState() {

						@Override
						public String getContentType() { return mimeType;}

						@Override
						public byte[] getData() { return BinaryFileData.this.getData();}
						
						@Override
						public Time lastModifiedTime() { return Time.valueOf(lastModified);}
					};
				}
			};
			res.setCacheable(true); // Not often changed
			Application.get().getSharedResources().add(filename, res);
		}
		
		return new ResourceReference(filename);
	}
}
