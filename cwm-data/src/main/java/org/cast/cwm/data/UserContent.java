/*
 * Copyright 2011-2019 CAST, Inc.
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.string.Strings;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A UserContent object represents a single chunk of user input.
 * 
 * It is an audited object, so it can be modified at will and the 
 * history of revisions will be available in the audit table.
 *
 * Most applications will need to extend this in order to connect user content
 * to application-specific contexts; eg, the content is responding to something
 * or placed somewhere.
 */
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes={
		@Index(columnList="user_id")
})
@DiscriminatorColumn(discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue(value="-")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of={"id","dataType","lastUpdated"})
public class UserContent extends PersistedObject {

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@ManyToOne(optional=false)
	private User user;
	
	@Type(type="org.cast.cwm.data.ResponseTypeHibernateType")
	private IContentType dataType;
	
	/**
	 * Date when content was originally created.
	 *
	 * Private in order to force using setter which does sanity checking.
	 */
	private Date createDate;
	
	/**
	 * Date of last change to the actual content.
	 * This field will be updated to the current time when certain other fields are modified.
	 *
	 * Private in order to force using setter which does sanity checking.
	 */
	private Date lastUpdated;
	
	/**
	 * Optional sorting field if a set of responses needs to be
	 * ordered by something other than date, etc.
	 * 
	 * Note: At this time, no guarantee is made regarding uniqueness
	 * or whether this value is even set.
	 * 
	 * Note: Because of "valid" and "invalid" responses, no guarantee
	 * is made regarding the sequence of numbers.  Values may be skipped.
	 *
	 * Private to force use of setter, which also updates lastUpdated.
	 */
	private Integer sortOrder;
	
	/**
	 * Short content, if appropriate for the content type.
	 * Could be numeric, plain text, HTML, and SVG.
	 *
	 * Private to force use of setter, which also updates lastUpdated.
	 */
	@Column(columnDefinition="TEXT")
	private String text;
	
	/**
	 * Supplemental title for the content.  This can be used as a 
	 * caption for an SVG Image, description for an upload, etc.
	 *
	 * Private to force use of setter, which also updates lastUpdated.
	 */
	@Column(columnDefinition="TEXT")
	private String title;

	/**
	 * Point to byte data for file-type content (eg uploaded document or audio)
	 *
	 * Private to force use of setter, which also updates lastUpdated.
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@Cascade({CascadeType.ALL})
	private BinaryFileData primaryFile;

	/**
	 * Point to supporting files (e.g. uploads to a drawing)
	 *
	 * Private to force use of setter, which also updates lastUpdated.
	 */
	@ManyToMany
	@JoinTable(inverseJoinColumns={@JoinColumn(name="file_id")})
	private Set<BinaryFileData> secondaryFiles = new HashSet<>();
	
	public UserContent() { /* No Arg Constructor for DataStore */ }
	
	public UserContent(User author, IContentType dataType) {
		this.user = author;
		this.dataType = dataType;
		this.createDate = new Date();
		this.lastUpdated = this.createDate;
	}
	
	/**
	 * Sets the createDate field, as well as the lastUpdated field.
	 * This is because lastUpdated should never be earlier than createDate and should always be set.
	 * @param createDate when this object was originally created
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
		if (lastUpdated == null || (createDate != null && createDate.after(lastUpdated)))
			lastUpdated = createDate;
	}
	
	/**
	 * Sets the text value, as well as the lastUpdated field if the text has changed.
	 * This enforces that lastUpdated will track any changes to the text.
	 * @param text text content to be stored
	 */
	public void setText(String text) {
		if (!Strings.isEqual(this.text, text))
			this.lastUpdated = new Date();
		this.text = text;
	}

	/**
	 * Sets the title value, as well as the lastUpdated field if the title has changed.
	 * This enforces that lastUpdated will track any changes to the title.
	 * @param title text to set
	 */
	public void setTitle(String title) {
		if (!Strings.isEqual(this.title, title))
			this.lastUpdated = new Date();
		this.title = title;
	}

	/**
	 * Sets the primaryFile value, as well as the lastUpdated field if the file has changed.
	 * This enforces that lastUpdated will track any changes to the primaryFile.
	 * @param primaryFile value to set
	 */
	public void setPrimaryFile(BinaryFileData primaryFile) {
		if (!Objects.equals(this.primaryFile, primaryFile))
			this.lastUpdated = new Date();
		this.primaryFile = primaryFile;
	}

	/**
	 * Sets the secondaryFiles value, as well as the lastUpdated field if the file has changed.
	 * This enforces that lastUpdated will track any changes to the primaryFile.
	 * @param secondaryFiles value to set
	 */
	public void setSecondaryFiles(Set<BinaryFileData> secondaryFiles) {
		if (!Objects.equals(this.secondaryFiles, secondaryFiles))
			this.lastUpdated = new Date();
		this.secondaryFiles = secondaryFiles;
	}

	/**
	 * Check whether this object has content that is considered empty based on its data type.
	 * Will probably need overriding if you define data types beyond ones that simply use "text"
	 * or a primaryFile to contain their data.
	 * @return true if the object contains no significant content.
	 */
	public boolean isEmpty() {
		if (dataType == null)
			throw new IllegalStateException("UserContent dataType is null");
		// Check for text that is not null, empty, or whitespace.
		if (!StringUtils.isBlank(this.getText()))
			return false;
		// Check for blob content
		return this.getPrimaryFile() == null;
	}

}
