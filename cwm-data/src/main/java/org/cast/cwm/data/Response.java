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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

/**
 * <p>
 * A Response represents a single chunk of user input; a response to 
 * a {@link Prompt} object.  Each Response refers to an active 
 * {@link ResponseData} object for its actual content; there may be 
 * inactive ResponseData objects that record a history of changes
 * to this Response.
 * </p>
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
@ToString(of={"id","type","lastUpdated"})
public class Response extends PersistedObject {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;

	@Index(name="response_prompt_idx")
	@ManyToOne
	private Prompt prompt;
	
	@Index(name="response_user_idx")
	@ManyToOne(optional=false)
	private User user;
	
	@Enumerated(EnumType.STRING)
	private ResponseType type;
	
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
	
	@ManyToOne
	@Cascade(CascadeType.ALL)
	private ResponseData responseData; // Latest Response Data
	
	@OneToMany(mappedBy="response", fetch=FetchType.LAZY)
	@Cascade(CascadeType.DELETE)
	@Setter(AccessLevel.NONE)
	private Set<ResponseData> allResponseData; // If you delete a response, all history is deleted.
	
	/**
	 * Supporting files (e.g. uploads to a drawing)
	 */
	@ManyToMany
	@JoinTable(name="response_file", inverseJoinColumns={@JoinColumn(name="file_id")})
	private Set<BinaryFileData> files = new HashSet<BinaryFileData>(); 
	
	private boolean valid = true;
	
	public Response() { /* No Arg Constructor for DataStore */ }
	
	public Response(User author, ResponseType t, Prompt p) {
		this.user = author;
		this.type = t;
		this.prompt = p;
		this.createDate = new Date();
	}
	
	/**
	 * Generates a new {@link ResponseData} object to be used with
	 * this {@link Response}.  This Response object will automatically
	 * point to the new ResponseData object, so calling this method will
	 * effectively "erase" the previous ResponseData.
	 * 
	 * TODO: It would be cool to tie this in with a setText() method so that
	 * Response would work properly with PropertyModels and Forms.  Perhaps
	 * a transient ResponseData that is generated once per request and setText(),
	 * setScore(), etc all use this transient field that is persisted as the
	 * new ResponseData.  This would allow us to drop all the methods in
	 * ResponseService.
	 * 
	 * @return
	 */
	public ResponseData getNewResponseDataObject() {
		responseData = new ResponseData(this);
		this.lastUpdated = responseData.getCreateDate();
		return responseData;
	}
	
	/**
	 * Fall-through methods to ResponseData  
	 */
	public String getText() {
		return getResponseData() == null ? null : getResponseData().getText();
	}
	
	public int getScore() {
		return getResponseData() == null ? 0 : getResponseData().getScore();
	}
	
	public int getAttempted() {
		return getResponseData() == null ? 0 : getResponseData().getAttempted();
	}
	
	public int getTotal() {
		return getResponseData() == null ? 1 : getResponseData().getTotal();
	}
}
