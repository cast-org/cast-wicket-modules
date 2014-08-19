/*
 * Copyright 2011-2014 CAST, Inc.
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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.string.Strings;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * A UserContent object represents a single chunk of user input.
 * Most often it is a response to a {@link Prompt} object.
 * 
 * It is an audited object, so it can be modified at will and the 
 * history of revisions will be available in the audit table.
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

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@ManyToOne(optional=false)
	private User user;
	
	@Type(type="org.cast.cwm.data.ResponseTypeHibernateType")
	private IResponseType dataType;
	
	/**
	 * Date when content was originally created.
	 */
	private Date createDate;
	
	/**
	 * Date of last change to the actual content.
	 * This field will be updated to the current time when certain other fields are modified. 
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
	 */
	private Integer sortOrder;
	
	/**
	 * Short content, if appropriate for the content type.
	 * Could be numeric, plain text, HTML, and SVG.
	 */
	@Column(columnDefinition="TEXT")
	private String text;
	
	/**
	 * Supplemental title for the content.  This can be used as a 
	 * caption for an SVG Image, description for an upload, etc.
	 */
	@Column(columnDefinition="TEXT")
	private String title;

	/**
	 * Point to byte data for file-type content (eg uploaded document or audio)
	 */
	// TODO: Is this the proper cascade?  
	// TODO: Should we avoid deletion to allow for re-use? If so, how do BinaryFileData objects get deleted?
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@Cascade({CascadeType.ALL})
	private BinaryFileData primaryFile;

	/**
	 * Point to supporting files (e.g. uploads to a drawing)
	 */
	@ManyToMany
	@JoinTable(inverseJoinColumns={@JoinColumn(name="file_id")})
	private Set<BinaryFileData> secondaryFiles = new HashSet<BinaryFileData>();
	
	/**
	 * Evaluations of this content, if any.
	 */
	@OneToMany(mappedBy="userContent")
	@Cascade({CascadeType.ALL})
	private Set<Evaluation> evaluations;
	
	/**
	 * Related event - eg the "post" or "save" that resulted in this contnet getting stored or updated.
	 */
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Event event;
	
	public UserContent() { /* No Arg Constructor for DataStore */ }
	
	public UserContent(User author, IResponseType dataType) {
		this.user = author;
		this.dataType = dataType;
		this.createDate = new Date();
		this.lastUpdated = this.createDate;
	}
	
	/**
	 * Sets the createDate field, as well as the lastUpdated field.
	 * This is because lastUpdated should never be earlier than createDate and should always be set.
	 * @param createDate
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
		if (lastUpdated == null || (createDate != null && createDate.after(lastUpdated)))
			lastUpdated = createDate;
	}
	
	/**
	 * Sets the text value, as well as the lastUpdated field if the text has changed.
	 * This enforces that lastUpdated will track any changes to the text.
	 * @param text
	 */
	public void setText(String text) {
		if (!Strings.isEqual(this.text, text))
			this.lastUpdated = new Date();
		this.text = text;
	}

	/**
	 * Sets the title value, as well as the lastUpdated field if the title has changed.
	 * This enforces that lastUpdated will track any changes to the title.
	 * @param text
	 */
	public void setTitle(String title) {
		if (!Strings.isEqual(this.title, title))
			this.lastUpdated = new Date();
		this.title = title;
	}

	/**
	 * Sets the primaryFile value, as well as the lastUpdated field if the file has changed.
	 * This enforces that lastUpdated will track any changes to the primaryFile.
	 * @param text
	 */
	public void setPrimaryFile(BinaryFileData primaryFile) {
		if (!ObjectUtils.equals(this.primaryFile, primaryFile))
			this.lastUpdated = new Date();
		this.primaryFile = primaryFile;
	}
	
	/**
	 * Check whether this object has content that is considered empty based on its data type.
	 * This incorporates rules and esoteric knowledge about a few commonly-used data types.
	 * Probably needs rethinking to make this more general somehow.
	 * @return
	 */
	public boolean isEmpty() {
		if (dataType == null)
			throw new IllegalStateException("UserContent dataType is null");
		String dtName = dataType.getName();
		if (dtName.equals("TEXT") ||
				dtName.equals("SINGLE_SELECT")) {
			if (this.getText() == null || (StringUtils.isBlank(this.getText()))) {
					return true;
			}
			return false;
		}
		else if (dtName.equals(("SVG"))) {
			if (this.getText() == null ||
				(StringUtils.isBlank(text))  ||  
				(this.getText().trim().replaceAll("\\s[\\s]*", "").equals("<svgwidth=\"535\"height=\"325\"xmlns=\"http://www.w3.org/2000/svg\"><gdisplay=\"inline\"><title>Layer1</title></g></svg>")) || 
				(this.getText().trim().replaceAll("\\s[\\s]*", "").equals("<svgwidth=\"535\"height=\"325\"xmlns=\"http://www.w3.org/2000/svg\"><!--CreatedwithSVG-edit-http://svg-edit.googlecode.com/--><g><title>Layer1</title></g></svg>"))
				) {		
				return true;
			}
			return false;
		}
		else if (dtName.equals(("AUDIO"))) {
			if (this.getPrimaryFile() == null) {
					return true;
			}
			return false;
		}
		else if (dtName.equals(("ARTIMAGE"))) {
			return false;
		}
		else {
			return true;
		}
		
	}

}
