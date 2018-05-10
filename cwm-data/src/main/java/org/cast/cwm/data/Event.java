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
package org.cast.cwm.data;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IEventType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

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
 * @see org.cast.cwm.service.IEventService
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of={"id","type","startTime"})
public class Event extends PersistedObject {

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;

	/**
	 * User whose action created this event.
	 */
	@ManyToOne(optional=false)
	protected User user;

	/**
	 * Login session that contains this Event.
	 */
	@ManyToOne(optional=false)
	protected LoginSession loginSession;

	/**
	 * Time when the event occurred (or started, for events with duration).
	 * (In server's time zone).
	 */
	@Column(nullable=false)
	protected Date startTime;

	/**
	 * Time of the event end, if it is an event with measured duration.
	 */
	@Column(nullable=true)
	protected Date endTime;

	/**
	 * Number of milliseconds of the event's duration that are considered "inactive".
	 * For page-view events, LoggedWebPage will set this to the time that the page's window
	 * was not focused.
	 */
	protected long inactiveDuration = 0L;

	/**
	 * For page view events, the time in ms that the browser took to load this page.
	 */
	@Column(nullable=true)
	protected Long loadTime;

	/**
	 * A type designation for this event.
	 * A few types are required by CWM but the application generally is expected to define its own taxonomy.
	 */
	@Column(nullable=false)
	@Type(type="org.cast.cwm.data.EventTypeHibernateType")
	protected IEventType type;

	/**
	 * Any additional information about this event.
	 */
	@Column(columnDefinition="TEXT")
	protected String detail;

	/**
	 * Name of the page on which this event occurred.
	 */
	protected String page;
	
	/**
	 * The wicket ID path of the component that was clicked to generate this event. 
	 */
	protected String componentPath;

	/**
	 * ID of a UserContent object connected to this event, if there is one.
	 * Not an actual reference to the object, since UserContent objects can be deleted while Events should never be.
	 */
	protected Long userContentId;

	/**
	 * Add a string-valued key=value pair to the "detail" string of this event.
	 * @param key the type of information being stored
	 * @param value the value for this event
	 * @return this
	 */
	public Event addDetail(String key, String value) {
		detail = String.format("%s%s=\"%s\"",
				Strings.isNullOrEmpty(detail) ? "" : detail + ", ",
				key, value);
		return this;
	}

	/**
	 * Add a numeric-valued key=value pair to the "detail" string of this event.
	 * @param key the type of information being stored
	 * @param value the value for this event
	 * @return this
	 */
	public Event addDetail(String key, Long value) {
		detail = String.format("%s%s=%d",
				Strings.isNullOrEmpty(detail) ? "" : detail + ", ",
				key, value);
		return this;
	}

	/**
	 * Called just before saving Event to database to set various fields
	 * whose values are predictable.  Override as necessary for you application.
	 */
	public void setDefaultValues() {
		if (startTime == null)
			startTime = new Date();
		if (loginSession == null)
			loginSession = CwmSession.get().getLoginSession();
		if ((user == null) && (loginSession != null))
			user = loginSession.getUser();
	}

	/**
	 * Return the recorded duration of the event, in milliseconds.
	 * If the start time or end time is unknown, will return 0.
	 *
	 * @return milliseconds between start and end time of event; 0 if unknown.
	 */
	public long getDuration() {
		if (endTime == null || startTime == null)
			return 0;
		else
			return endTime.getTime() - startTime.getTime();
	}

	public long getActiveDuration() {
		return getDuration() - inactiveDuration;
	}

}
