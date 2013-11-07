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
package org.cast.cwm.data.builders;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.CriteriaBuilder;

import org.apache.wicket.model.IDetachable;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * A simple CriteriaBuilder that locates a Prompt by its identifier.
 * 
 * @author bgoldowsky
 *
 */
@Getter
@Setter
public class PromptCriteriaBuilder implements CriteriaBuilder, IDetachable {

	private static final long serialVersionUID = 1L;
	
	private String identifier;
	
	public PromptCriteriaBuilder(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public void build(Criteria criteria) {
		if (identifier != null) {
			criteria.add(Restrictions.eq("identifier", identifier));
			criteria.setMaxResults(1); // identifier is unique
		}
		criteria.setCacheable(true);
	}

	@Override
	public void detach() {
		// Nothing to detach, yet, but following other Criteria Builder Conventions.
	}

}