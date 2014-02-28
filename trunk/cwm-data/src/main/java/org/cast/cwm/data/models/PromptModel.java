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
package org.cast.cwm.data.models;

import net.databinder.models.hib.HibernateObjectModel;

import org.cast.cwm.data.Prompt;

/**
 * A simple Model to look up a Prompt by its identifier.
 * It can automatically create a Prompt with the given identifier if you set autoCreate to true.
 * 
 * @author bgoldowsky
 *
 */
public class PromptModel extends HibernateObjectModel<Prompt> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Initialize model with an existing Datastore Id.
	 * 
	 * @param id
	 */
	public PromptModel (Long id) {
		super(Prompt.class, id);
	}
	
	public PromptModel (Prompt p) {
		super(p);
	}

}
