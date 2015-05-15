/*
 * Copyright 2011-2015 CAST, Inc.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

/**
 * <p>
 * The actual content for a given {@link Response}.  Each time
 * a Response changes, a new ResponseData object is created and 
 * the Response object maintains a link to the latest ResponseData
 * object.  The 'type' of content is determined by the owning
 * Response object.
 * </p>
 * @author jbrookover
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of="id")
public class ResponseData extends PersistedObject {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@Index(name="responsedata_response_idx")
	@ManyToOne(optional = false)
	private Response response;
	
	@Column(nullable=false)
	private Date createDate;
	
	/**
	 * Content for this ResponseData.  This is often repurposed to include
	 * plain text, HTML, and SVG.
	 */
	@Column(columnDefinition="TEXT")
	private String text;
	
	/**
	 * Supplemental title for the content.  This can be repurposed as a 
	 * caption for an SVG Image, description for an upload, etc.
	 */
	@Column(columnDefinition="TEXT")
	private String title;
	
	// TODO: Is this the proper cascade and relationship?
	// TODO: How do BinaryFileData objects get deleted?
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@Cascade({CascadeType.ALL})
	private BinaryFileData binaryFileData;

	@Column(columnDefinition="int default 0")
	private int score = 0; // Points earned
	
	@Column(columnDefinition="int default 0")
	private int attempted = 0; // Points attempted (if skipping is available)
	
	@Column(columnDefinition="int default 1")
	private int total = 1; // Total points available
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Event event;
	
	protected ResponseData() { /* No Arg Constructor for DataStore */ }

	protected ResponseData(Response response) {
		this.response = response;
		this.createDate = new Date();
	}
	
	/**
	 * Convenience method that calls getBinaryFileData().getData();
	 * 
	 * @return the stored byte array
	 */
	public byte[] getBytes() {
		if (getBinaryFileData() == null)
			return null;
		else
			return getBinaryFileData().getData();
	}	

}
