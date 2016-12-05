/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.models.hib;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;

/**
 * <h1>SortableHibernateProvider</h1>
 * <i>Copyright (C) 2008 The Scripps Research Institute</i>
 * <p>A HibernateProvider extension that implements ISortableDataProvider so it can be used with a DefaultDataTable or an AjaxFallbackDefaultDataTable
 * The ICriteriaBuilder that handles the sorting should also implement ISortStateLocator (such as CriteriaSorter).</p>
 * 
 * @author Mark Southern (southern at scripps dot edu)
 * 
 * TODO: allow the second parameter of ISortableDataProvider to be also parameterized here, rather than insisting on String.
 * 
 */

public class SortableHibernateProvider<T> extends HibernateProvider<T> implements ISortableDataProvider<T,String> {

	private ISortStateLocator sortStateLocator = null;

    private ISortState sortState = null;

    public SortableHibernateProvider(Class<T> objectClass, ICriteriaBuilder criteriaBuilder) {
        super(objectClass, criteriaBuilder);
        if (criteriaBuilder instanceof ISortStateLocator)
            sortStateLocator = (ISortStateLocator) criteriaBuilder;
    }

    @Override
	public ISortState getSortState() {
        return (sortStateLocator != null) ? sortStateLocator.getSortState() : sortState;
    }

    public void setSortState(ISortState state) {
        sortStateLocator = null;
        this.sortState = state;
    }
}
