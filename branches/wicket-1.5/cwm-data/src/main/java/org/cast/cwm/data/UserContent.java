/*
 * Copyright 2011-2013 CAST, Inc.
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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * A UserContent object represents a single chunk of user input.
 * Most often it is a response to a {@link Prompt} object.
 */
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
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

	@Index(name="usercontent_prompt_idx")
	@ManyToOne
	private Prompt prompt;
	
	@Index(name="usercontent_user_idx")
	@ManyToOne(optional=false)
	private User user;
	
	@Type(type="org.cast.cwm.data.ResponseTypeHibernateType")
	private IResponseType dataType;
	
	private Date createDate;
	
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
	
	public UserContent(User author, IResponseType dataType, Prompt prompt) {
		this.user = author;
		this.dataType = dataType;
		this.prompt = prompt;
		this.createDate = new Date();
	}

}
