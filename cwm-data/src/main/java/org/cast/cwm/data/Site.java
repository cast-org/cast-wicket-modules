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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.markup.html.list.ListView;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.*;

/**
 * A named location and group of {@link Period} objects.  Conceptually, this should
 * be used to organize Periods and {@link User}s into a 'school' location.
 * 
 * @author jbrookover
 *
 */
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter
@Setter
@ToString(of={"id", "name"})
public class Site extends PersistedObject implements Comparable<Site> {
  
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;

	/**
	 * User-visible name
	 */
	private String name;

	/**
	 * Anonymous identifier for research purposes.
	 */
	@Column(unique = true, nullable = false)
	private String siteId;
	
	private String location;
	
	@Column(name="language", columnDefinition="CHAR(2)", nullable=false)
	private String language = "en";
	
	@Column(name="country", columnDefinition="CHAR(2)", nullable=false)
	private String country = "US";
	
	private String timezone;

	@OneToMany(mappedBy="site")
	@Cascade(CascadeType.ALL)
	private Set<Period> periods = new HashSet<Period>();
	
	public Site() { /* No Arg Constructor for Datastore */ }
	
	/**
	 * Returns an unmodifiable list of periods in this site.  This
	 * is a convenience method to be used with {@link ListView}
	 * components in Wicket.
	 * 
	 * @return read-only list of periods in natural order.
	 */
	public List<Period> getPeriodsAsSortedReadOnlyList() {
		List<Period> list = new ArrayList<Period>(periods);
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}

	// Sort order for sites is based on their name, or ID if names are equal
	@Override
	public int compareTo(Site other) {
		if (this.equals(other))
			return 0;

		int nameDiff = ObjectUtils.compare(this.name, other.name);
		if (nameDiff != 0)
			return nameDiff;

		return ObjectUtils.compare(this.id, other.id);
	}

}