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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.hibernate.annotations.GenericGenerator;

/**
 * A representation of a single login session for a single user.
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
@ToString(of={"user","startTime"})
public class LoginSession extends PersistedObject {
  
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	@Column(nullable=false)
	private Date startTime;
	
	@Column(nullable=true)
	private Date endTime;
	
	@ManyToOne(optional=false)
	private User user;
	
	private String sessionId;
	
	private String userAgent;
	private String ipAddress;
	private Integer timezoneOffset;
	private Integer screenWidth;
	private Integer screenHeight;
	private String platform;
	private Boolean cookiesEnabled;
	private String locale;
	private String flashVersion;
	
	/** Return duration of this LoginSession in seconds */
	public Long getDuration() {
		if (startTime != null && endTime != null)
			return( (endTime.getTime() - startTime.getTime()) / 1000L );
		if (startTime == null)
		throw new IllegalStateException("Start time is null");
		return( (new Date().getTime() - startTime.getTime()) / 1000L );
	}

	// Override setters to avoid throwing an exception for very long user-agent strings and the like.
	// Yes, I have seen user agent strings longer than 255 bytes.
	public void setUserAgent (String userAgent) {
		this.userAgent = (userAgent==null ? null : trimToLength (userAgent, 255));
	}
	
	public void setPlatform (String platform) {
		this.platform = (platform==null ? null : trimToLength (platform, 255));
	}
	
	protected String trimToLength (String string, int length) {
		if (string.length() < length)
			return string;
		return string.substring(0, length);
	}
	
}
