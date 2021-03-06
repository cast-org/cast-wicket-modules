/*
 * Copyright 2011-2016 CAST, Inc.
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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.cast.cwm.CwmSession;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

/**
 * <p>
 * A logged event of a {@link User}'s action in the program.  Events are
 * generated and stored by an {@link org.cast.cwm.service.IEventService} implementation.
 * </p>
 * <p>
 * In general, unless specifically viewing the event log, the event table
 * should not be queried, particularly for application operation.
 * </p>
 * @author jbrookover
 * @see {@link org.cast.cwm.service.IEventService}
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of={"id","type","insertTime"})
public class Event extends PersistedObject {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;

	@ManyToOne(optional=false)
	protected User user;
	
	@ManyToOne(optional=false)
	protected LoginSession loginSession;
	
	protected boolean hasResponses = false;
	
	protected boolean hasUserContent = false;
	
	@OneToMany(mappedBy="event", fetch=FetchType.LAZY)
	protected Set<ResponseData> responseData = new HashSet<ResponseData>();
	
	@Column(nullable=false)
	protected Date insertTime;
	
	@Column(nullable=false)
	protected String type;
	
	@Column(columnDefinition="TEXT")
	protected String detail;
	
	protected String page;
	
	/**
	 * The wicket ID path of the component that was clicked to generate this event. 
	 */
	protected String componentPath;
	
	/** Called just before saving Event to database to set various fields
	 * whose values are predictable.  Override as necessary for you application.
	 */
	public void setDefaultValues() {
		if (insertTime == null)
			insertTime = new Date();
		if (loginSession == null)
			loginSession = CwmSession.get().getLoginSession();
		if ((user == null) && (loginSession != null))
			user = loginSession.getUser();
	}
	
	// Not auto-generated in Lombok 0.10.0
	public boolean hasResponses() {
		return hasResponses;
	}
	
}
